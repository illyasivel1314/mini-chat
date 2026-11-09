package com.cetc28s.minichatjava.controller.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户注册请求（F01：手机号为唯一标识）
 */
@Getter
@Setter
public class UserRegisterRequest {
    // 手机号（11 位，全局唯一，登录唯一标识）
    @NotNull
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    // 密码（前端进行md5加密）
    @NotNull
    private String password;

    // 姓名（2-20 字符）
    @NotNull
    @Size(min = 2, max = 20, message = "姓名长度需为 2-20 字符")
    private String name;

    // 工号（可选，≤20 字符）
    @Size(max = 20, message = "工号长度不能超过 20 字符")
    private String account;

    // 岗位（PositionEnum）
    @NotNull
    private int position;

    // 职责（DutyEnum：0项目负责人/1开发员/2测试员）
    @NotNull
    private int duty;

    // 详细信息（自由文本）
    private String detail;

    // 职位概述（历史字段）
    private String positionDescription;

    // 头像
    private String avatar;
}
