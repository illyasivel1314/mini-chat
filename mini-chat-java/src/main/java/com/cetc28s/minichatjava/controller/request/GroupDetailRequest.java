package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

@Data
public class GroupDetailRequest {

    private String groupId;

    private String name;

    private String announcement;

    private String remark;

}
