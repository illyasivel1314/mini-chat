package com.cetc28s.minichatjava.model.dto;

import lombok.Data;

@Data
public class ChatFriendDto {

    // 接收者id
    private String receiveId;

    // 类型(group/friend)
    private int chatType;

    // 名称
    private String name;

    // 头像
    private String avatar;

    // 最新时间
    private long lastTime;

    // 最新消息
    private String lastContent;

    // 未读数量
    private int unread;
}
