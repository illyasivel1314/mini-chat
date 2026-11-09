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
 * 版本实体（任务协同根节点）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "version_table")
public class VersionEntity implements Serializable {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /**
     * 版本名称（必填）
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 计划发布时间戳（毫秒）
     */
    @Column(name = "plan_release_time")
    private Long planReleaseTime;

    /**
     * 状态：0进行中/1已关闭/2已取消
     */
    @Column(name = "status", nullable = false)
    private int status;

    /**
     * 创建人ID（仅项目负责人可创建）
     */
    @Column(name = "creator_id", nullable = false)
    private String creatorId;

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
