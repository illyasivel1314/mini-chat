package com.cetc28s.minichatjava.model.enums;

import lombok.Getter;

/**
 * 流转动作（flow_record_table.action）
 */
@Getter
public enum FlowActionEnum {

    CREATE("创建", 0),
    TRANSFER("转包", 1)
    ;

    private String key;

    private int value;

    FlowActionEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

}
