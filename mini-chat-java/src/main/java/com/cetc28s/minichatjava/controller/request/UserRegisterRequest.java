package com.cetc28s.minichatjava.controller.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 用户注册请求
 */
@Getter
@Setter
public class UserRegisterRequest {
    // 工号
    @NotNull
    private String account;

    // 密码（前端进行md5加密）
    @NotNull
    private String password;

    // 姓名
    @NotNull
    private String name;

    // 职位
    @NotNull
    private int position;

    // 职位概述
    private String positionDescription;

    // 头像
    private String avatar;
}
