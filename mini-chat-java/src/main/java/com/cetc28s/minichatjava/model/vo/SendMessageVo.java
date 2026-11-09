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

    /**
     * 引用消息ID（深度1层）
     */
    private String replyTo;

    /**
     * @提及的用户ID列表（JSON数组字符串）
     */
    private String atUsers;

    /**
     * 是否@所有人
     */
    private Boolean atAll;

    /**
     * 转发来源消息ID
     */
    private String forwardFrom;

}
