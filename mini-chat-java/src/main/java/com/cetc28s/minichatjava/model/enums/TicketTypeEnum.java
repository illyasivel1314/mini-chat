package com.cetc28s.minichatjava.model.enums;

import lombok.Getter;

/**
 * 单据类型（flow_record_table.ticket_type）
 */
@Getter
public enum TicketTypeEnum {

    REQUIREMENT("需求单", 0),
    BUG("bug单", 1)
    ;

    private String key;

    private int value;

    TicketTypeEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

}
