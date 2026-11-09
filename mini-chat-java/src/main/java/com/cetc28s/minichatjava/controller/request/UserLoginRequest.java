package com.cetc28s.minichatjava.controller.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 用户登陆（F01：手机号 + MD5 密码）
 */
@Getter
@Setter
public class UserLoginRequest {
    // 手机号
    @NotNull
    private String phone;

    // 密码（已通过md5进行加密）
    @NotNull
    private String password;
}
