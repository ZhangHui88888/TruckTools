package com.trucktools.system.service;

import com.trucktools.system.dto.*;
import com.trucktools.system.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request, String ip);

    /**
     * 用户注册
     */
    Long register(RegisterRequest request);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 获取用户信息
     */
    UserVO getUserProfile(Long userId);

    /**
     * 更新用户信息
     */
    void updateProfile(Long userId, UserVO userVO);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 根据ID获取用户
     */
    User getById(Long id);

    /**
     * 根据用户名获取用户
     */
    User getByUsername(String username);

    /**
     * 发送密码重置邮件
     */
    void sendResetPasswordEmail(String email);

    /**
     * 重置密码
     */
    void resetPassword(String token, String newPassword);
}

