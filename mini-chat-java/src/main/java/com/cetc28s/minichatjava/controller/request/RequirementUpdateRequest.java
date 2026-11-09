package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 编辑需求单请求（F09：仅负责人）
 */
@Data
public class RequirementUpdateRequest {

    @NotNull
    private String requirementId;

    private String title;

    private String content;
}
