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
    @Column(name = "id", length = 32)
    private String id;

    /**
     * 会话ID（sessionWindowId）
     */
    @Column(name = "conversation_id")
    private String conversationId;

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
     * 聊天类型：PRIVATE / GROUP / SYSTEM / SELF
     */
    @Column(name = "chat_type", nullable = false)
    private int chatType;

    /**
     * 内容类型：TEXT / IMAGE / FILE / EMOJI
     */
    @Column(name = "content_type", nullable = false)
    private int contentType;

    /**
     * 消息内容（JSON Schema，GIF含 emojiKey=分组/文件名）
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 引用消息ID（引用深度仅1层）
     */
    @Column(name = "reply_to")
    private String replyTo;

    /**
     * @提及的用户ID列表（JSON数组）
     */
    @Column(name = "at_users", columnDefinition = "TEXT")
    private String atUsers;

    /**
     * 是否@所有人
     */
    @Column(name = "at_all", nullable = false)
    private Boolean atAll = false;

    /**
     * 转发来源消息ID（单条转发，标记来自XX）
     */
    @Column(name = "forward_from")
    private String forwardFrom;

    /**
     * 是否撤回
     */
    @Column(name = "recalled", nullable = false)
    private Boolean recalled = false;

    /**
     * 撤回时间戳（毫秒）
     */
    @Column(name = "recall_time")
    private Long recallTime;

    /**
     * 是否已读（遗留字段，建议迁移到 conversation_member_table）
     */
    @Column(name = "is_read")
    private Boolean isRead = false;

    /**
     * 消息发送时间戳（毫秒）
     */
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;
}
