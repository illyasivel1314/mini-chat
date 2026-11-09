package com.cetc28s.minichatjava.service;

import com.cetc28s.minichatjava.dao.ConversationDao;
import com.cetc28s.minichatjava.dao.ConversationMemberDao;
import com.cetc28s.minichatjava.dao.GroupMemberDao;
import com.cetc28s.minichatjava.model.entity.ConversationEntity;
import com.cetc28s.minichatjava.model.entity.ConversationMemberEntity;
import com.cetc28s.minichatjava.model.entity.GroupMemberEntity;
import com.cetc28s.minichatjava.model.enums.ConversationTypeEnum;
import com.cetc28s.minichatjava.utils.CommonUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话与已读位点服务（PRD F03/F05：conversation_table + conversation_member_table）
 */
@Service
public class ConversationService {

    private static final Gson GSON = new Gson();

    @Resource
    private ConversationDao conversationDao;

    @Resource
    private ConversationMemberDao conversationMemberDao;

    @Resource
    private GroupMemberDao groupMemberDao;

    /**
     * 生成确定性会话ID。
     * 私聊/自发/系统会话用 md5 压缩到 32 字符以内；群聊直接用 groupId。
     */
    public String resolveConversationId(int chatType, String senderId, String receiverId) {
        if (chatType == ConversationTypeEnum.GROUP.getValue()) {
            // 群聊：会话即群组，1:1
            return receiverId;
        }
        if (chatType == ConversationTypeEnum.SELF.getValue()) {
            return md5("self|" + senderId);
        }
        if (chatType == ConversationTypeEnum.SYSTEM.getValue()) {
            return md5("system|" + receiverId);
        }
        // 私聊：用户对等，双方复用同一会话（字典序固定）
        String a = senderId, b = receiverId;
        if (a.compareTo(b) > 0) {
            String t = a; a = b; b = t;
        }
        return md5("p|" + a + "|" + b);
    }

    /**
     * 确保会话存在（不存在则创建），返回会话ID。
     */
    @Transactional
    public ConversationEntity ensureConversation(int chatType, String conversationId, String groupId) {
        return conversationDao.findById(conversationId).orElseGet(() -> {
            ConversationEntity entity = ConversationEntity.builder()
                    .id(conversationId)
                    .type(chatType)
                    .groupId(chatType == ConversationTypeEnum.GROUP.getValue() ? groupId : null)
                    .createTime(CommonUtil.generateTimestamp())
                    .updateTime(CommonUtil.generateTimestamp())
                    .build();
            return conversationDao.save(entity);
        });
    }

    /**
     * 消息发送后：确保会话与成员行存在，并为接收方累计未读 / [有人@我] 计数。
     */
    @Transactional
    public void onMessageSent(int chatType, String conversationId, String senderId,
                              String receiverId, String atUsersJson, boolean atAll) {
        ConversationEntity conversation = ensureConversation(chatType, conversationId,
                chatType == ConversationTypeEnum.GROUP.getValue() ? receiverId : null);
        conversation.setUpdateTime(CommonUtil.generateTimestamp());
        conversationDao.save(conversation);

        long now = CommonUtil.generateTimestamp();
        Set<String> mentioned = parseAtUsers(atUsersJson);

        // 发送者自身的成员行（保证会话列表能定位；系统消息发送者为空则不建）
        if (senderId != null && !senderId.isEmpty()) {
            ensureMember(conversationId, senderId, now);
        }

        // 接收方集合
        List<String> recipients = resolveRecipients(chatType, senderId, receiverId);
        for (String recipient : recipients) {
            ConversationMemberEntity member = ensureMember(conversationId, recipient, now);
            member.setUnreadCount((member.getUnreadCount() == null ? 0 : member.getUnreadCount()) + 1);
            if (atAll || mentioned.contains(recipient)) {
                member.setAtCount((member.getAtCount() == null ? 0 : member.getAtCount()) + 1);
            }
            conversationMemberDao.save(member);
        }
    }

    /**
     * 打开会话：清空本视角未读与 [有人@我] 计数，更新已读位点。
     */
    @Transactional
    public void markRead(String conversationId, String userId) {
        ConversationMemberEntity member = conversationMemberDao.findByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            return;
        }
        member.setUnreadCount(0);
        member.setAtCount(0);
        member.setLastReadTime(CommonUtil.generateTimestamp());
        conversationMemberDao.save(member);
    }

    /**
     * 获取群聊会话内全部成员ID（含发送者），用于逐人私聊推送。
     */
    public List<String> listGroupMemberIds(String groupId) {
        List<GroupMemberEntity> members = groupMemberDao.findByGroupId(groupId);
        if (members == null) {
            return Collections.emptyList();
        }
        return members.stream()
                .map(GroupMemberEntity::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    private ConversationMemberEntity ensureMember(String conversationId, String userId, long now) {
        ConversationMemberEntity member = conversationMemberDao.findByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            return member;
        }
        return conversationMemberDao.save(ConversationMemberEntity.builder()
                .id(CommonUtil.generateUuid())
                .conversationId(conversationId)
                .userId(userId)
                .lastReadTime(null)
                .unreadCount(0)
                .atCount(0)
                .createTime(now)
                .build());
    }

    private List<String> resolveRecipients(int chatType, String senderId, String receiverId) {
        if (chatType == ConversationTypeEnum.GROUP.getValue()) {
            List<GroupMemberEntity> members = groupMemberDao.findByGroupId(receiverId);
            if (members == null) {
                return Collections.emptyList();
            }
            return members.stream()
                    .map(GroupMemberEntity::getUserId)
                    .filter(uid -> !uid.equals(senderId))
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (chatType == ConversationTypeEnum.PRIVATE.getValue()
                || chatType == ConversationTypeEnum.SYSTEM.getValue()) {
            // 私聊、系统消息：单个接收人累计未读
            return Collections.singletonList(receiverId);
        }
        // 自发会话不累计未读
        return Collections.emptyList();
    }

    private Set<String> parseAtUsers(String atUsersJson) {
        if (atUsersJson == null || atUsersJson.trim().isEmpty()) {
            return Collections.emptySet();
        }
        try {
            List<String> list = GSON.fromJson(atUsersJson, new TypeToken<List<String>>() {}.getType());
            return list == null ? Collections.emptySet() : new HashSet<>(list);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }
}
