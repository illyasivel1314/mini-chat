package com.cetc28s.minichatjava.model.dto;

import lombok.Data;

@Data
public class UserLoginDto {

    private String id;

    // 姓名
    private String name;

    // 拼音
    private String pinyin;

    // 头像
    private String avatar;

    // 职位
    private int position;

    // 职位概述
    private String positionDescription;

    // token
    private String token;
}
