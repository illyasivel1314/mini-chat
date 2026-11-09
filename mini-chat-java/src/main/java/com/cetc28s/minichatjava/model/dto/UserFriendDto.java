package com.cetc28s.minichatjava.model.dto;

import lombok.Data;

@Data
public class UserFriendDto {

    private String id;

    private String avatar;

    private String name;

    private String pinyin;

    // 工号
    private String account;

    // 手机号
    private String phone;

    private int position;

    // 职责
    private int duty;

    private String positionDescription;

    // 详细信息
    private String detail;

    // 逻辑删除标记（仅 admin 后台使用）
    private Boolean deleted;
}
