package com.cetc28s.minichatjava.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;

/**
 * 会话成员与已读位点实体（用户 × 会话）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversation_member_table",
        uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"}))
public class ConversationMemberEntity implements Serializable {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /**
     * 会话ID
     */
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 本视角已读位点（毫秒时间戳，打开会话即更新）
     */
    @Column(name = "last_read_time")
    private Long lastReadTime;

    /**
     * 未读消息数（会话列表角标）
     */
    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;

    /**
     * 未清除的[有人@我]计数（红色角标）
     */
    @Column(name = "at_count", nullable = false)
    private Integer atCount = 0;

    /**
     * 加入会话时间戳（毫秒）
     */
    @Column(name = "create_time", nullable = false)
    private Long createTime;
}
