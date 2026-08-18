package com.cetc28s.minichatjava.model.dto;

import lombok.Data;

@Data
public class UserFriendDto {

    private String id;

    private String avatar;

    private String name;

    private String pinyin;

    private int position;

    private String positionDescription;
}
