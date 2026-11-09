package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 修改单据状态请求（F09/F10：已完成/已取消；bug 单可同时填原因/方案/结果截图）
 */
@Data
public class TicketStatusRequest {

    // 单据类型：0需求单/1bug单
    @NotNull
    private int ticketType;

    @NotNull
    private String ticketId;

    @NotNull
    private int status;

    // bug 原因（当前负责人填写）
    private String reason;

    // 解决方案（当前负责人填写）
    private String solution;

    // 结果截图（文件地址）
    private String resultScreenshot;
}
