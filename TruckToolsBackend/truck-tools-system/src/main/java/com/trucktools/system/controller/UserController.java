package com.trucktools.system.controller;

import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.system.dto.UserVO;
import com.trucktools.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户个人资料管理")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserVO userVO = userService.getUserProfile(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserVO userVO) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(userId, userVO);
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // TODO: 实现文件上传到OSS
        Long userId = SecurityUtils.getCurrentUserId();
        String avatarUrl = "https://example.com/avatar/" + userId + ".jpg";
        
        UserVO userVO = new UserVO();
        userVO.setAvatar(avatarUrl);
        userService.updateProfile(userId, userVO);
        
        return Result.success(Map.of("avatarUrl", avatarUrl));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.successMsg("密码修改成功");
    }
}

