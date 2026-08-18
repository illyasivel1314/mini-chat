package com.cetc28s.minichatjava.service;

import com.cetc28s.minichatjava.dao.ChatGroupDao;
import com.cetc28s.minichatjava.dao.GroupMemberDao;
import com.cetc28s.minichatjava.dao.MessageDao;
import com.cetc28s.minichatjava.dao.UserDao;
import com.cetc28s.minichatjava.exception.BusinessException;
import com.cetc28s.minichatjava.exception.ErrorCodeEnum;
import com.cetc28s.minichatjava.model.converter.MessageConverter;
import com.cetc28s.minichatjava.model.dto.ChatFriendDto;
import com.cetc28s.minichatjava.model.dto.ChatMessageDto;
import com.cetc28s.minichatjava.model.entity.ChatGroupEntity;
import com.cetc28s.minichatjava.model.entity.GroupMemberEntity;
import com.cetc28s.minichatjava.model.entity.MessageEntity;
import com.cetc28s.minichatjava.model.entity.UserEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消息服务层
 */
@Service
public class MessageService {

    @Resource
    private MessageDao messageDao;

    @Resource
    private UserDao userDao;

    @Resource
    private GroupMemberDao groupMemberDao;

    @Resource
    private ChatGroupDao chatGroupDao;

    /**
     * 保存消息并返回填充了发送者信息的 DTO
     */
    public void saveAndBuildMessage(ChatMessageDto chatMessageDto) {
        // 构建实体保存到数据库
        MessageEntity messageEntity = MessageConverter.MAPPER.convertMessageEntity(chatMessageDto);
        messageDao.save(messageEntity);
    }

    /**
     * 获取私聊历史消息
     */
    public Slice<ChatMessageDto> getPrivateHistory(String currentUserId, String userId, Pageable pageable) {
        Slice<MessageEntity> messages = messageDao.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestampDesc(
                currentUserId, userId, currentUserId, userId, pageable
        );
        // 当前用户
        UserEntity currentUser = userDao.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_NOT_FOUND));;
        // 好友
        UserEntity user = userDao.findById(userId).orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_NOT_FOUND));;

        return messages.map(message -> message.getSenderId().equals(currentUserId)
                ? MessageConverter.MAPPER.convertChatMessageDto(message, currentUser)
                : MessageConverter.MAPPER.convertChatMessageDto(message, user)
        );
    }

    /**
     * 获取群聊历史消息
     */
    public Slice<ChatMessageDto> getGroupHistory(String groupId, Pageable pageable) {
        Slice<MessageEntity> messages = messageDao.findByReceiverIdOrderByTimestampDesc(groupId, pageable);
        // 好友列表
        Map<String, UserEntity> userEntityMap = userDao.findAll()
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (k1, k2) -> k1));

        return messages.map(message -> {
            UserEntity user = userEntityMap.get(message.getSenderId());
            if (Objects.isNull(user)) {
                user = new UserEntity();
                user.setName("未知用户");
            }
            return MessageConverter.MAPPER.convertChatMessageDto(message, user);
        });
    }

    /**
     * 获取用户未读消息数量
     */
    public long getUnreadCount(String userId) {
        return messageDao.countByReceiverIdAndIsReadFalse(userId);
    }

    /**
     * 标记消息为已读
     */
    public void markAsRead(String receiveId, String senderId) {

        List<MessageEntity> unreadMessages = messageDao.findByReceiverIdAndSenderIdAndIsReadFalse(receiveId, senderId);
        for (MessageEntity msg : unreadMessages) {
            msg.setIsRead(true);
            messageDao.save(msg);
        }
    }

    /**
     * 将群消息转化为聊天列表格式
     * @param currentUserId 当前用户
     * @return 聊天列表
     */
    private List<ChatFriendDto> acquireGroupChatFriendList(String currentUserId) {
        // 获取加入的群聊
        List<GroupMemberEntity> groupMemberList = groupMemberDao.findByUserId(currentUserId);
        if (CollectionUtils.isEmpty(groupMemberList)) {
            return Collections.emptyList();
        }
        // 获取群号id
        List<String> groupMemberIdList = groupMemberList.stream().map(GroupMemberEntity::getGroupId).collect(Collectors.toList());
        // 读取最新消息（批量查询的sql略显复杂）
        List<MessageEntity> messageEntityList = groupMemberIdList
                .stream()
                .map(value -> messageDao.findFirstByReceiverIdOrderByTimestampDesc(value))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(messageEntityList)) {
            return Collections.emptyList();
        }
        // 获取群聊信息
        List<String> chatGroupIdList = groupMemberList.stream().map(GroupMemberEntity::getGroupId).collect(Collectors.toList());
        Map<String, ChatGroupEntity> chatGroupEntityMap = chatGroupDao.findAllByIdIn(chatGroupIdList)
                .stream()
                .collect(Collectors.toMap(ChatGroupEntity::getId, Function.identity(), (k1, k2) -> k1));

        return messageEntityList
                .stream()
                .map(message -> MessageConverter.MAPPER.convertChatFriendDto(message, chatGroupEntityMap.get(message.getReceiverId())))
                .collect(Collectors.toList());
    }

    /**
     * 将好友消息转化为聊天列表格式
     * @param currentUserId 当前用户
     * @return 聊天列表
     */
    private List<ChatFriendDto> acquireUserChatFirendList(String currentUserId) {
        // 获取所有的好友列表
        List<UserEntity> userEntityList = userDao.findAll();
        if (CollectionUtils.isEmpty(userEntityList)) {
            return Collections.emptyList();
        }
        // 获取好友id
        List<String> userIdList = userEntityList.stream().map(UserEntity::getId).collect(Collectors.toList());
        // 读取最新消息
        List<MessageEntity> messageEntityList = userIdList
                .stream()
                .map(value -> {
                    MessageEntity firstMessage = messageDao.findFirstBySenderIdAndReceiverIdOrderByTimestampDesc(currentUserId, value);
                    MessageEntity secondMessage = messageDao.findFirstBySenderIdAndReceiverIdOrderByTimestampDesc(value, currentUserId);
                    if (Objects.isNull(firstMessage) && Objects.isNull(secondMessage)) {
                        return null;
                    } else if (Objects.isNull(firstMessage)) {
                        return secondMessage;
                    } else if (Objects.isNull(secondMessage)) {
                        return firstMessage;
                    }
                    return firstMessage.getTimestamp() > secondMessage.getTimestamp() ? firstMessage : secondMessage;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(messageEntityList)) {
            return Collections.emptyList();
        }
        // 获取用户信息
        List<String> chatFriendIdList = messageEntityList
                .stream()
                .map(message -> message.getSenderId().equals(currentUserId) ? message.getReceiverId() : message.getSenderId())
                .collect(Collectors.toList());
        Map<String, UserEntity> userEntityMap = userDao.findAllByIdIn(chatFriendIdList)
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (k1, k2) -> k1));

        return messageEntityList
                .stream()
                .map(message -> {
                    String friendId = message.getSenderId().equals(currentUserId) ? message.getReceiverId() : message.getSenderId();
                    long unreadNum = messageDao.countAllBySenderIdAndReceiverIdAndIsReadFalse(friendId, currentUserId);
                    return MessageConverter.MAPPER.convertChatFriendDto(message, userEntityMap.get(friendId), unreadNum);
                })
                .collect(Collectors.toList());

    }

    /**
     * 获取有聊天记录的好友列表
     */
    public List<ChatFriendDto> getChatMessageFriend(String currentUserId) {
        List<ChatFriendDto> chatFriendDtoList = new ArrayList<>();
        // 1、获取相关的群聊信息
        chatFriendDtoList.addAll(acquireGroupChatFriendList(currentUserId));
        // 2、获取好友相关的私聊信息
        chatFriendDtoList.addAll(acquireUserChatFirendList(currentUserId));
        // 排序
        chatFriendDtoList.sort(Comparator.comparing(ChatFriendDto::getLastTime).reversed());
        return chatFriendDtoList;
    }
}
