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
 * 需求单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "requirement_table")
public class RequirementEntity implements Serializable {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /**
     * 所属版本ID
     */
    @Column(name = "version_id", nullable = false)
    private String versionId;

    /**
     * 标题（必填）
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * 需求内容（必填）
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 负责人ID（当前，转包时变更）
     */
    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    /**
     * 创建人ID
     */
    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    /**
     * 状态：0开发中/1已完成/2已取消
     */
    @Column(name = "status", nullable = false)
    private int status;

    /**
     * 创建时间戳（毫秒）
     */
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    /**
     * 更新时间戳（毫秒）
     */
    @Column(name = "update_time")
    private Long updateTime;
}
