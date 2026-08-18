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
    private String id;

    // 工号
    @Column(name = "account")
    private String account;

    // 密码
    @Column(name = "password")
    private String password;

    /**
     * 姓名
     */
    @Column(name = "name")
    private String name;

    /**
     * 拼音
     */
    @Column(name = "pinyin")
    private String pinyin;

    /**
     * 头像
     */
    @Column(name = "profile")
    private String avatar;

    /**
     * 职位
     */
    @Column(name = "position")
    private String position;

    /**
     * 职位概述
     */
    @Column(name = "position_description")
    private String positionDescription;
}
