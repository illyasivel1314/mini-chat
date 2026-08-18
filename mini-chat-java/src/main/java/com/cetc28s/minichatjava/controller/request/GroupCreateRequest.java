package com.cetc28s.minichatjava.controller.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GroupCreateRequest {

    @NotNull
    private String name;

    // 头像
    @NotNull
    private String avatar;

    @Min(2)
    private List<String> userIdList;
}
