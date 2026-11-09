package com.cetc28s.minichatjava.service;

import com.cetc28s.minichatjava.dao.MessageDao;
import com.cetc28s.minichatjava.model.dto.ChatMessageDto;
import com.cetc28s.minichatjava.model.entity.MessageEntity;
import com.cetc28s.minichatjava.model.enums.ChatTypeEnum;
import com.cetc28s.minichatjava.model.enums.ContentTypeEnum;
import com.cetc28s.minichatjava.model.vo.MessageTextVo;
import com.cetc28s.minichatjava.utils.CommonUtil;
import com.google.gson.Gson;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 系统消息服务（F11：任务变更通知，复用聊天 STOMP 通道）
 */
@Service
public class SystemNotifyService {

    private static final Gson GSON = new Gson();

    @Resource
    private MessageDao messageDao;

    @Resource
    private ConversationService conversationService;

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向单个用户推送系统消息（落库 + 未读 + STOMP 推送）
     */
    @Transactional
    public void notify(String userId, String text) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        long now = CommonUtil.generateTimestamp();
        String conversationId = conversationService.resolveConversationId(
                ChatTypeEnum.SYSTEM.getValue(), "", userId);

        MessageEntity message = MessageEntity.builder()
                .id(CommonUtil.generateUuid())
                .conversationId(conversationId)
                .senderId("")
                .receiverId(userId)
                .chatType(ChatTypeEnum.SYSTEM.getValue())
                .contentType(ContentTypeEnum.TEXT.getValue())
                .content(GSON.toJson(new MessageTextVo(text)))
                .atAll(false)
                .recalled(false)
                .isRead(false)
                .timestamp(now)
                .build();
        messageDao.save(message);

        // 系统消息计入未读
        conversationService.onMessageSent(ChatTypeEnum.SYSTEM.getValue(), conversationId,
                "", userId, null, false);

        ChatMessageDto dto = ChatMessageDto.builder()
                .id(message.getId())
                .senderId("")
                .senderName("系统")
                .receiverId(userId)
                .conversationId(conversationId)
                .chatType(ChatTypeEnum.SYSTEM.getValue())
                .contentType(ContentTypeEnum.TEXT.getValue())
                .content(message.getContent())
                .timestamp(now)
                .isRead(false)
                .build();
        // 系统消息广播：前端订阅 /queue/system，收到后按 receiverId 过滤出属于自己的
        messagingTemplate.convertAndSend("/queue/system", dto);
    }
}
