package com.cetc28s.minichatjava.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息传输对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 发送者ID
     */
    private String senderId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**+
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者ID
     */
    private String receiverId;

    /**
     * 聊天类型：PRIVATE / GROUP / SYSTEM
     */
    private int chatType;

    /**
     * 内容类型：TEXT / IMAGE / FILE / EMOJI
     */
    private int contentType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 是否已读
     */
    private Boolean isRead;
}
