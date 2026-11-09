package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建 bug 单请求（F10）
 */
@Data
public class BugCreateRequest {

    @NotNull
    private String versionId;

    @NotNull
    private String title;

    @NotNull
    private String detail;

    // 界面截图（文件地址）
    private String screenshot;

    @NotNull
    private String ownerId;
}
