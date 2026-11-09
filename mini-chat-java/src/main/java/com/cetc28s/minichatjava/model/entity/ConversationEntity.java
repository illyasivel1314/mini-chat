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
 * 会话实体（聊天窗口，sessionWindowId）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversation_table")
public class ConversationEntity implements Serializable {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /**
     * 会话类型：0私聊/1群聊/2自发会话(我的文件)/3系统消息
     */
    @Column(name = "type", nullable = false)
    private int type;

    /**
     * 群组ID（type=1 时关联 chat_group_table.id）
     */
    @Column(name = "group_id")
    private String groupId;

    /**
     * 创建时间戳（毫秒）
     */
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    /**
     * 最近活跃时间戳（毫秒）
     */
    @Column(name = "update_time")
    private Long updateTime;
}
