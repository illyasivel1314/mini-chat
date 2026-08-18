package com.cetc28s.minichatjava.model.vo;

import lombok.Data;

@Data
public class SendMessageVo {

    // 接收者ID
    private String receiverId;

    /**
     * 聊天类型：PRIVATE / GROUP
     */
    private int chatType;

    /**
     * 内容类型：TEXT / IMAGE / FILE / EMOJI
     */
    private int contentType;

    /**
     * 消息内容(json格式)
     */
    private String content;

}
