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
 * 聊天群组实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_group_table")
public class ChatGroupEntity implements Serializable {

    @Id
    private String id;

    /**
     * 群组名称
     */
    @Column(name = "group_name", nullable = false)
    private String groupName;

    /**
     * 创建者ID
     */
    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    /**
     * 群组头像
     */
    @Column(name = "group_avatar")
    private String groupAvatar;

    /**
     * 群公告
     */
    @Column(name = "group_notice")
    private String groupNotice;

    /**
     * 群备注
     */
    @Column(name = "group_remark")
    private String groupRemark;

    /**
     * 创建时间戳（毫秒）
     */
    @Column(name = "create_time", nullable = false)
    private Long createTime;
}
