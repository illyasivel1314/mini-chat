package com.cetc28s.minichatjava.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupDetailDto {

    private String id;

    // 群组名称
    private String name;

    //创建者ID
    private String creatorId;

    // 群组头像
    private String groupAvatar;

    // 群公告
    private String announcement;

    // 群备注
    private String remark;

    // 人数
    private List<MemberDetail> members;

    @Data
    public static class MemberDetail {

        private String id;
        // 用户名
        private String name;
        // 用户账号
        private String account;
        // 用户头像
        private String avatar;
    }
}
