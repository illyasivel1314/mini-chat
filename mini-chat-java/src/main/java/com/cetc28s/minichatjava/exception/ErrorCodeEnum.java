package com.cetc28s.minichatjava.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum ErrorCodeEnum {
    USER_NOT_FOUND(1001, "用户不存在"),
    INVALID_PARAM(1002, "参数错误"),
    DUPLICATE_ACCOUNT(1003, "工号已存在"),
    INVALID_PASSWORD(1004, "密码错误"),
    FILE_EMPTY(1005, "文件为空"),
    FILE_UPLOAD_FAIL(1006, "文件上传失败"),
    DUPLICATE_PHONE(1007, "该手机号已注册"),
    PERMISSION_DENIED(1008, "无权限操作"),
    VERSION_NOT_FOUND(1009, "版本不存在"),
    TICKET_NOT_FOUND(1010, "单据不存在"),
    VERSION_HAS_TICKETS(1011, "版本下存在单据，无法删除"),
    TICKET_NOT_EDITABLE(1012, "单据当前不可编辑"),
    ACCOUNT_DELETED(1013, "账号已注销，请联系管理员"),
    ADMIN_AUTH_FAILED(1014, "管理员账号或密码错误"),
    RECALL_TIMEOUT(1015, "超过 2 分钟的消息不可撤回"),
    IMAGE_TOO_LARGE(1016, "图片大小不能超过 10MB"),
    FILE_TOO_LARGE(1017, "文件大小不能超过 50MB"),
    ;

    private Integer code;
    private String message;

    ErrorCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
