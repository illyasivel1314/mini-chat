package com.cetc28s.minichatjava.model.enums;

import com.cetc28s.minichatjava.exception.BusinessException;
import com.cetc28s.minichatjava.exception.ErrorCodeEnum;
import lombok.Getter;

/**
 * 聊天类型
 */
@Getter
public enum ChatTypeEnum {
    /**
     * 私聊
     */
    PRIVATE("private", 0),
    /**
     * 群聊
     */
    GROUP("group", 1),
    /**
     * 系统消息
     */
    SYSTEM("system", 2);

    private String key;

    private int value;

    ChatTypeEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public static ChatTypeEnum acquireChatTypeByValue(int value) {
        for (ChatTypeEnum chatType : ChatTypeEnum.values()) {
            if (chatType.value == value) {
                return chatType;
            }
        }
        throw new BusinessException(ErrorCodeEnum.INVALID_PARAM);
    }
}
