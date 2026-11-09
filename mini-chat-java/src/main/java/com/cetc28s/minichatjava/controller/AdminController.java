package com.cetc28s.minichatjava.controller;

import com.cetc28s.minichatjava.controller.request.AdminLoginRequest;
import com.cetc28s.minichatjava.exception.CommonResult;
import com.cetc28s.minichatjava.model.dto.UserFriendDto;
import com.cetc28s.minichatjava.service.AdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 账号管理后台控制层（F12）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    /**
     * admin 登录
     */
    @PostMapping("/login")
    public CommonResult<String> login(@Validated @RequestBody AdminLoginRequest request) {
        return CommonResult.success(adminService.login(request.getUsername(), request.getPassword()));
    }

    /**
     * 账号查询
     */
    @GetMapping("/user/list")
    public List<UserFriendDto> list(@RequestParam(required = false) String keyword) {
        return adminService.queryUsers(keyword);
    }

    /**
     * 重置密码为 123456
     */
    @PostMapping("/user/reset")
    public void resetPassword(@RequestParam String userId) {
        adminService.resetPassword(userId);
    }

    /**
     * 逻辑删除账号
     */
    @PostMapping("/user/delete")
    public void delete(@RequestParam String userId) {
        adminService.setDeleted(userId, true);
    }

    /**
     * 恢复账号
     */
    @PostMapping("/user/restore")
    public void restore(@RequestParam String userId) {
        adminService.setDeleted(userId, false);
    }
}
