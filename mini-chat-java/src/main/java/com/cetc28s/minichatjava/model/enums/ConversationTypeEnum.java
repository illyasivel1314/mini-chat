package com.cetc28s.minichatjava.model.enums;

import lombok.Getter;

/**
 * 会话类型（conversation_table.type）
 */
@Getter
public enum ConversationTypeEnum {

    /**
     * 私聊（用户对等，双方复用同一会话）
     */
    PRIVATE("private", 0),
    /**
     * 群聊（关联 group_id）
     */
    GROUP("group", 1),
    /**
     * 系统消息（每用户一个）
     */
    SYSTEM("system", 2),
    /**
     * 自发会话（“我的文件”，每用户一个）
     */
    SELF("self", 3);

    private String key;

    private int value;

    ConversationTypeEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

}
