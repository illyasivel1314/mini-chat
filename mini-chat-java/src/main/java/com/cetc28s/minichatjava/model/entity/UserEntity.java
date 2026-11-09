package com.cetc28s.minichatjava.model.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name="user_table")
public class UserEntity implements Serializable  {
    @Id
    @Column(name = "id", length = 32)
    private String id;

    /**
     * 手机号（11位，登录唯一标识）
     */
    @Column(name = "phone", nullable = false, unique = true, length = 11)
    private String phone;

    /**
     * 工号（可选，≤20字符，不做唯一约束）
     */
    @Column(name = "account", length = 20)
    private String account;

    /**
     * 密码（前端MD5后存储，不加盐）
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 姓名
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 拼音（搜索用）
     */
    @Column(name = "pinyin")
    private String pinyin;

    /**
     * 头像（默认头像）
     */
    @Column(name = "avatar")
    private String avatar;

    /**
     * 岗位（PositionEnum：0负责人/1Java开发/2前端开发/3C端开发/4三方测试/5内部测试）
     */
    @Column(name = "position", nullable = false)
    private int position;

    /**
     * 职责（DutyEnum：0项目负责人/1开发员/2测试员，任务协同权限依据）
     */
    @Column(name = "duty", nullable = false)
    private int duty;

    /**
     * 详细信息（自由文本）
     */
    @Column(name = "detail")
    private String detail;

    /**
     * 职位概述（历史字段）
     */
    @Column(name = "position_description")
    private String positionDescription;

    /**
     * 逻辑删除：0正常/1删除
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

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
