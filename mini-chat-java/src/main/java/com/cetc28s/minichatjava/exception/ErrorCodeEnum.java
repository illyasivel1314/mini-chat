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
    ;

    private Integer code;
    private String message;

    ErrorCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
