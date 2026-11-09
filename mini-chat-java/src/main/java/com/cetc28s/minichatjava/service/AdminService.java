package com.cetc28s.minichatjava.service;

import com.cetc28s.minichatjava.dao.UserDao;
import com.cetc28s.minichatjava.exception.BusinessException;
import com.cetc28s.minichatjava.exception.ErrorCodeEnum;
import com.cetc28s.minichatjava.model.converter.UserConverter;
import com.cetc28s.minichatjava.model.dto.UserFriendDto;
import com.cetc28s.minichatjava.model.entity.UserEntity;
import com.cetc28s.minichatjava.utils.CommonUtil;
import com.cetc28s.minichatjava.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账号管理后台服务（F12：账号查询 / 重置密码 / 逻辑删除与恢复）
 */
@Service
public class AdminService {

    private static final String RESET_PASSWORD = "123456";

    @Resource
    private UserDao userDao;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * admin 登录（独立账号体系，与注册用户隔离）
     */
    public String login(String username, String password) {
        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            throw new BusinessException(ErrorCodeEnum.ADMIN_AUTH_FAILED);
        }
        return jwtTokenUtil.generateToken(adminUsername);
    }

    /**
     * 账号查询（按手机号/姓名/工号，含已删除账号）
     */
    public List<UserFriendDto> queryUsers(String keyword) {
        return userDao.findAll().stream()
                .filter(u -> keyword == null || keyword.trim().isEmpty()
                        || contains(u.getPhone(), keyword)
                        || contains(u.getName(), keyword)
                        || contains(u.getAccount(), keyword))
                .map(UserConverter.MAPPER::convertUserFriendDto)
                .collect(Collectors.toList());
    }

    /**
     * 重置密码为 123456（按注册同款 MD5 口径入库）
     */
    public void resetPassword(String userId) {
        UserEntity user = userDao.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_NOT_FOUND));
        user.setPassword(DigestUtils.md5DigestAsHex(RESET_PASSWORD.getBytes(StandardCharsets.UTF_8)));
        user.setUpdateTime(CommonUtil.generateTimestamp());
        userDao.save(user);
    }

    /**
     * 逻辑删除 / 恢复
     */
    public void setDeleted(String userId, boolean deleted) {
        UserEntity user = userDao.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_NOT_FOUND));
        user.setDeleted(deleted);
        user.setUpdateTime(CommonUtil.generateTimestamp());
        userDao.save(user);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }
}
