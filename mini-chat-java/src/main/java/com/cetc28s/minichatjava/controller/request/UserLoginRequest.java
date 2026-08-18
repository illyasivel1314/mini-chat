package com.cetc28s.minichatjava.controller.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 用户登陆
 */
@Getter
@Setter
public class UserLoginRequest {
    // 账号
    @NotNull
    private String account;

    // 密码（已通过md5进行加密）
    @NotNull
    private String password;
}
