package com.cetc28s.minichatjava.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 聊天消息实体
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "message_table")
public class MessageEntity implements Serializable {

    @Id
    private String id;

    /**
     * 发送者ID
     */
    @Column(name = "sender_id", nullable = false)
    private String senderId;

    /**
     * 接收者ID
     */
    @Column(name = "receiver_id")
    private String receiverId;

    /**
     * 聊天类型：PRIVATE / GROUP / SYSTEM
     */
    @Column(name = "chat_type", nullable = false)
    private int chatType;

    /**
     * 内容类型：TEXT / IMAGE / FILE / EMOJI
     */
    @Column(name = "content_type", nullable = false)
    private int contentType;

    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 消息发送时间戳（毫秒）
     */
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    /**
     * 是否已读
     */
    @Column(name = "is_read")
    private Boolean isRead = false;
}
