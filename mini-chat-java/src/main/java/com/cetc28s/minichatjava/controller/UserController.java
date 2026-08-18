package com.cetc28s.minichatjava.controller;

import com.cetc28s.minichatjava.controller.request.*;
import com.cetc28s.minichatjava.model.context.UserContext;
import com.cetc28s.minichatjava.model.dto.GroupDetailDto;
import com.cetc28s.minichatjava.model.dto.UserFriendDto;
import com.cetc28s.minichatjava.model.dto.UserGroupDto;
import com.cetc28s.minichatjava.model.dto.UserLoginDto;
import com.cetc28s.minichatjava.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户认证控制层
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public UserLoginDto login(@Validated @RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public void register(@Validated @RequestBody UserRegisterRequest request) {
        userService.register(request);
    }

    /**
     * 获取所有的好友
     */
    @GetMapping("/friend/list")
    public List<UserFriendDto> acquireAllFriend() {
        return userService.acquireAllFriend();
    }

    /**
     * 获取加入的群聊
     */
    @GetMapping("/group/list")
    public List<UserGroupDto> acquireAllGroup() {
        String currentUserId = UserContext.getCurrentUser();
        return userService.acquireAllGroup(currentUserId);
    }

    /**
     * 创建群聊
     */
    @PostMapping("/group/create")
    public void createGroup(@RequestBody GroupCreateRequest groupCreateRequest) {
        String currentUserId = UserContext.getCurrentUser();
        userService.createGroup(currentUserId, groupCreateRequest);
    }

    /**
     * 获取群聊明细
     */
    @GetMapping("/group/detail")
    public GroupDetailDto acquireGroupDetail(@RequestParam @NotNull String groupId) {
        return userService.acquireGroupDetail(groupId);
    }

    /**
     * 获取群聊人数
     */
    @GetMapping("/group/members")
    public int acquireGroupMembers(@RequestParam @NotNull String groupId) {
        return userService.acquireGroupMembers(groupId);
    }

    /**
     * 添加好友加入群聊
     */
    @PostMapping("/group/addMembers")
    public void addGroupMembers(@RequestBody GroupAddRequest groupAddRequest) {
        userService.addGroupMembers(groupAddRequest);
    }

    /**
     * 更新
     */
    @PostMapping("/group/update")
    public void updateGroupDetail(@RequestBody GroupDetailRequest groupDetailRequest) {
        userService.updateGroupDetail(groupDetailRequest);
    }

    /**
     * 离开
     */
    @GetMapping("/group/leave")
    public void leaveGroup(@RequestParam("groupId") String groupId, @RequestParam("userId") String userId) {
        userService.leaveGroup(groupId, userId);
    }
}
