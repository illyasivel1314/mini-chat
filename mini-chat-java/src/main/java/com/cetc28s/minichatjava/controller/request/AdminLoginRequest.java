package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * admin 后台登录请求（F12）
 */
@Data
public class AdminLoginRequest {

    @NotNull
    private String username;

    @NotNull
    private String password;
}
