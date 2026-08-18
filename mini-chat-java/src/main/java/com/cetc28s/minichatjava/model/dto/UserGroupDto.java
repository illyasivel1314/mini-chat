package com.cetc28s.minichatjava.model.dto;

import lombok.Data;

@Data
public class UserGroupDto {

    private String id;

    private String groupName;

    private String creatorId;

    private String groupAvatar;

    private String groupNotice;
}
