package com.cetc28s.minichatjava.model.enums;

import lombok.Getter;

/**
 * 版本状态（version_table.status）
 */
@Getter
public enum VersionStatusEnum {

    IN_PROGRESS("进行中", 0),
    CLOSED("已关闭", 1),
    CANCELLED("已取消", 2)
    ;

    private String key;

    private int value;

    VersionStatusEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

}
