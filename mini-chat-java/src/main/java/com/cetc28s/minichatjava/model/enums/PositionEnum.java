package com.cetc28s.minichatjava.model.enums;

import lombok.Getter;

/**
 * 岗位枚举（PRD F01：负责人 / Java 开发 / 前端开发 / C 端开发 / 三方测试 / 内部测试）
 */
@Getter
public enum PositionEnum {

    LEADER("负责人", 0),
    JAVA_DEVELOPER("Java 开发", 1),
    FRONT_END_DEVELOPER("前端开发", 2),
    C_DEVELOPER("C 端开发", 3),
    THIRD_PARTY_TESTER("三方测试", 4),
    INTERNAL_TESTER("内部测试", 5)
    ;

    private String key;

    private int value;

    PositionEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

}
