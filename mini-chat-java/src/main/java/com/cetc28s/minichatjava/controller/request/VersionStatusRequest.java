package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 修改版本状态请求（F08：关闭/取消）
 */
@Data
public class VersionStatusRequest {

    @NotNull
    private String versionId;

    @NotNull
    private int status;
}
