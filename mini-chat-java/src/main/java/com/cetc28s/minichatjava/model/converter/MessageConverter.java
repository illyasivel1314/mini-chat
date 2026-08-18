package com.cetc28s.minichatjava.model.converter;

import com.cetc28s.minichatjava.model.dto.ChatFriendDto;
import com.cetc28s.minichatjava.model.dto.ChatMessageDto;
import com.cetc28s.minichatjava.model.entity.ChatGroupEntity;
import com.cetc28s.minichatjava.model.entity.MessageEntity;
import com.cetc28s.minichatjava.model.entity.UserEntity;
import com.cetc28s.minichatjava.model.enums.ContentTypeEnum;
import com.cetc28s.minichatjava.model.vo.*;
import com.cetc28s.minichatjava.utils.CommonUtil;
import com.google.gson.Gson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageConverter {

    MessageConverter MAPPER = Mappers.getMapper(MessageConverter.class);

    @Mappings({
            @Mapping(target = "receiveId", source = "chatGroup.id"),
            @Mapping(target = "chatType", constant = "1"),
            @Mapping(target = "name", source = "chatGroup.groupName"),
            @Mapping(target = "avatar", source = "chatGroup.groupAvatar"),
            @Mapping(target = "lastTime", source = "message.timestamp"),
            @Mapping(target = "lastContent", source = "message", qualifiedByName = "contentFormat"),
            @Mapping(target = "unread", constant = "0"),
    })
    ChatFriendDto convertChatFriendDto(MessageEntity message, ChatGroupEntity chatGroup);

    @Mappings({
            @Mapping(target = "receiveId", source = "user.id"),
            @Mapping(target = "chatType", constant = "0"),
            @Mapping(target = "name", source = "user.name"),
            @Mapping(target = "avatar", source = "user.avatar"),
            @Mapping(target = "lastTime", source = "message.timestamp"),
            @Mapping(target = "lastContent", source = "message", qualifiedByName = "contentFormat"),
            @Mapping(target = "unread", source = "unreadNum"),
    })
    ChatFriendDto convertChatFriendDto(MessageEntity message, UserEntity user, long unreadNum);

    @Named("contentFormat")
    default String contentFormat(MessageEntity message) {
        Gson mapper = new Gson();
        if (ContentTypeEnum.TEXT.getValue() == message.getContentType()) {
            MessageTextVo messageTextVo = mapper.fromJson(message.getContent(), MessageTextVo.class);
            return messageTextVo.getContent();
        } else if (ContentTypeEnum.IMAGE.getValue() == message.getContentType()) {
            return "[图片]";
        } else if (ContentTypeEnum.FILE.getValue() == message.getContentType()) {
            MessageFileVo messageFileVo = mapper.fromJson(message.getContent(), MessageFileVo.class);
            return "[" +  messageFileVo.getFileName() + messageFileVo.getFileExtension() + "]";
        } else if (ContentTypeEnum.EMOJI.getValue() == message.getContentType()) {
            MessageEmojiVo messageEmojiVo = mapper.fromJson(message.getContent(), MessageEmojiVo.class);
            return "[" + messageEmojiVo.getEmojiName()  + "]";
        }
        return "";
    }

    @Named("generateUuid")
    default String generateUuid() {
        return CommonUtil.generateUuid();
    }

    @Named("generateTimestamp")
    default long generateTimestamp() {
        return CommonUtil.generateTimestamp();
    }

    @Mappings({
            @Mapping(target = "id", expression = "java(MAPPER.generateUuid())"),
            @Mapping(target = "senderId", source = "senderId"),
            @Mapping(target = "receiverId", source = "sendMessageVo.receiverId"),
            @Mapping(target = "chatType", source = "sendMessageVo.chatType"),
            @Mapping(target = "contentType", source = "sendMessageVo.contentType"),
            @Mapping(target = "content", source = "sendMessageVo.content"),
            @Mapping(target = "timestamp", expression = "java(MAPPER.generateTimestamp())"),
            @Mapping(target = "isRead", constant = "false"),
    })
    ChatMessageDto convertChatMessageDto(SendMessageVo sendMessageVo, String senderId);

    @Mappings({
            @Mapping(target = "id", source = "messageEntity.id"),
            @Mapping(target = "senderId", source = "userEntity.id"),
            @Mapping(target = "senderName", source = "userEntity.name"),
            @Mapping(target = "senderAvatar", source = "userEntity.avatar"),
            @Mapping(target = "receiverId", source = "messageEntity.receiverId"),
            @Mapping(target = "chatType", source = "messageEntity.chatType"),
            @Mapping(target = "contentType", source = "messageEntity.contentType"),
            @Mapping(target = "content", source = "messageEntity.content"),
            @Mapping(target = "timestamp", source = "messageEntity.timestamp"),
            @Mapping(target = "isRead", source = "messageEntity.isRead"),
    })
    ChatMessageDto convertChatMessageDto(MessageEntity messageEntity, UserEntity userEntity);

    @Mappings({})
    MessageEntity convertMessageEntity(ChatMessageDto chatMessageDto);
}
