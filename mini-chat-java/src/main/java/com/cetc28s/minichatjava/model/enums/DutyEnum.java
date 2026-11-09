package com.cetc28s.minichatjava.model.enums;

import lombok.Getter;

/**
 * 职责枚举（PRD：项目负责人 / 开发员 / 测试员，任务协同权限依据）
 */
@Getter
public enum DutyEnum {

    PROJECT_OWNER("项目负责人", 0),
    DEVELOPER("开发员", 1),
    TESTER("测试员", 2)
    ;

    private String key;

    private int value;

    DutyEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

}
