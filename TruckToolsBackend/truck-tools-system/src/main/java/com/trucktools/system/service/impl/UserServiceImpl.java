package com.trucktools.system.service.impl;

import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.system.dto.*;
import com.trucktools.system.entity.User;
import com.trucktools.system.mapper.UserMapper;
import com.trucktools.system.security.JwtTokenProvider;
import com.trucktools.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtTokenProvider tokenProvider;
    
    // Redis可选，开发环境可不启用
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    // 当Redis不可用时，使用内存存储（仅用于开发）
    private final Map<String, Object> memoryStore = new ConcurrentHashMap<>();

    private static final String RESET_TOKEN_PREFIX = "user:reset:";

    @Override
    public LoginResponse login(LoginRequest request, String ip) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            // 尝试用邮箱登录
            user = userMapper.selectByEmail(request.getUsername());
        }
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验密码
        if (!SecurityUtils.matchPassword(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        // 生成Token
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        // 构建响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .user(convertToVO(user))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        // 校验确认密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 校验用户名是否存在
        if (userMapper.existsByUsername(request.getUsername()) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 校验邮箱是否存在
        if (userMapper.existsByEmail(request.getEmail()) > 0) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(SecurityUtils.encryptPassword(request.getPassword()));
        user.setNickname(request.getUsername());
        user.setStatus(1);
        user.setEmailVerified(0);

        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());

        return user.getId();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证刷新Token
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 获取用户信息
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 生成新Token
        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .user(convertToVO(user))
                .build();
    }

    @Override
    public void logout() {
        // JWT是无状态的，可以在Redis中维护黑名单
        // 这里简单处理，前端清除Token即可
        log.info("用户登出");
    }

    @Override
    public UserVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserVO userVO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 更新可修改字段
        if (userVO.getNickname() != null) {
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getPhone() != null) {
            user.setPhone(userVO.getPhone());
        }
        if (userVO.getAvatar() != null) {
            user.setAvatar(userVO.getAvatar());
        }

        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 验证旧密码
        if (!SecurityUtils.matchPassword(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        userMapper.updateById(user);
        log.info("用户修改密码成功: {}", user.getUsername());
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public void sendResetPasswordEmail(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            // 为了安全，不提示邮箱是否存在
            return;
        }

        // 生成重置Token
        String token = java.util.UUID.randomUUID().toString();
        String key = RESET_TOKEN_PREFIX + token;
        
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, user.getId(), 1, TimeUnit.HOURS);
        } else {
            // 开发环境使用内存存储
            memoryStore.put(key, user.getId());
            log.warn("Redis不可用，使用内存存储重置Token（仅用于开发）");
        }

        // TODO: 发送重置邮件
        log.info("发送密码重置邮件到: {}, token: {}", email, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String token, String newPassword) {
        String key = RESET_TOKEN_PREFIX + token;
        Object userIdObj;
        
        if (redisTemplate != null) {
            userIdObj = redisTemplate.opsForValue().get(key);
        } else {
            userIdObj = memoryStore.get(key);
        }
        
        if (userIdObj == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = Long.valueOf(userIdObj.toString());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 更新密码
        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        userMapper.updateById(user);

        // 删除重置Token
        if (redisTemplate != null) {
            redisTemplate.delete(key);
        } else {
            memoryStore.remove(key);
        }
        log.info("用户重置密码成功: {}", user.getUsername());
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId().toString());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        return vo;
    }
}

