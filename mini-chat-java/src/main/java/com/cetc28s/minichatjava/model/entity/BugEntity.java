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
 * bug单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bug_table")
public class BugEntity implements Serializable {

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
     * bug操作明细（必填）
     */
    @Column(name = "detail", columnDefinition = "TEXT", nullable = false)
    private String detail;

    /**
     * bug界面截图（文件地址，可JSON数组）
     */
    @Column(name = "screenshot")
    private String screenshot;

    /**
     * 负责人ID（当前，转包时变更）
     */
    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    /**
     * 创建人ID（始终不变）
     */
    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    /**
     * bug原因（当前负责人填写）
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /**
     * 解决方案（当前负责人填写）
     */
    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;

    /**
     * 结果截图（文件地址）
     */
    @Column(name = "result_screenshot")
    private String resultScreenshot;

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
