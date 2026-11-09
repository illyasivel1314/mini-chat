package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 编辑 bug 单请求（F10：仅创建者编辑标题/明细/截图）
 */
@Data
public class BugUpdateRequest {

    @NotNull
    private String bugId;

    private String title;

    private String detail;

    private String screenshot;
}
