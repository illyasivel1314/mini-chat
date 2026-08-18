package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

@Data
public class GroupUpdateRequest {

    private String groupId;

    // 群名称
    private String name;

    // 群公告
    private String announcement;

    // 备注
    private String remark;
}
