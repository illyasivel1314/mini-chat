package com.cetc28s.minichatjava.model.enums;

import com.cetc28s.minichatjava.exception.BusinessException;
import com.cetc28s.minichatjava.exception.ErrorCodeEnum;
import lombok.Getter;

/**
 * 消息内容类型
 */
@Getter
public enum ContentTypeEnum {
    /**
     * 文本
     */
    TEXT("text", 0),
    /**
     * 图片
     */
    IMAGE("image", 1),
    /**
     * 文件
     */
    FILE("file", 2),
    /**
     * 表情包
     */
    EMOJI("emoji", 3);

    private String key;

    private int value;

    ContentTypeEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public static ContentTypeEnum acquireContentTypeByValue(int value) {
        for (ContentTypeEnum contentType : ContentTypeEnum.values()) {
            if (contentType.value == value) {
                return contentType;
            }
        }
        throw new BusinessException(ErrorCodeEnum.INVALID_PARAM);
    }

}
