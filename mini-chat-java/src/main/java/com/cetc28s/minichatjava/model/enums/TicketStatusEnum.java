package com.cetc28s.minichatjava.model.enums;

import lombok.Getter;

/**
 * 单据状态（需求单 / bug 单共用）
 */
@Getter
public enum TicketStatusEnum {

    IN_PROGRESS("开发中", 0),
    COMPLETED("已完成", 1),
    CANCELLED("已取消", 2)
    ;

    private String key;

    private int value;

    TicketStatusEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

}
