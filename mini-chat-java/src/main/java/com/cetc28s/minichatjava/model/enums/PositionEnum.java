package com.cetc28s.minichatjava.model.enums;

import lombok.Getter;

@Getter
public enum PositionEnum {

    FRONT_END_DEVELOPER("前端开发工程师", 0),
    JAVA_DEVELOPER("Java开发工程师", 1),
    C_DEVELOPER("C开发工程师", 2),
    TEXT_DEVELOPER("测试工程师", 3),
    OTHER_DEVELOPER("其他", 4)
    ;

    private String key;

    private int value;

    PositionEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

}
