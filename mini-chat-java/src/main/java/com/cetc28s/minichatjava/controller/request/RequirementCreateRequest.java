package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建需求单请求（F09）
 */
@Data
public class RequirementCreateRequest {

    @NotNull
    private String versionId;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private String ownerId;
}
