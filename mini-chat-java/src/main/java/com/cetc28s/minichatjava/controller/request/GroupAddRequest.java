package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import java.util.List;

@Data
public class GroupAddRequest {

    private String groupId;

    private List<String> userIdList;
}
