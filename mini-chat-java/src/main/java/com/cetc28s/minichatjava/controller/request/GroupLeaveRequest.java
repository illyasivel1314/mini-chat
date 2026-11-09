package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 退出群聊请求（F06：退群用户 ID 从 token 中获取）
 */
@Data
public class GroupLeaveRequest {

    @NotNull
    private String groupId;
}
