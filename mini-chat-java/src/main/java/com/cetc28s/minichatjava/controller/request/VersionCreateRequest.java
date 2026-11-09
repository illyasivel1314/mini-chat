package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建版本请求（F08）
 */
@Data
public class VersionCreateRequest {

    @NotNull
    private String name;

    // 描述
    private String description;

    // 计划发布时间戳（毫秒）
    private Long planReleaseTime;
}
