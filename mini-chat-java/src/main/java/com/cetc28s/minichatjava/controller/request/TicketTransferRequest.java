package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 转包请求（F09/F10）
 */
@Data
public class TicketTransferRequest {

    // 单据类型：0需求单/1bug单
    @NotNull
    private int ticketType;

    @NotNull
    private String ticketId;

    // 转入人ID
    @NotNull
    private String toUserId;

    // 备注（转包必填）
    @NotNull
    private String remark;
}
