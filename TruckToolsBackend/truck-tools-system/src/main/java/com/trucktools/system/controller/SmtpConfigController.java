package com.trucktools.system.controller;

import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.system.dto.SmtpConfigRequest;
import com.trucktools.system.dto.SmtpConfigVO;
import com.trucktools.system.service.SmtpConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SMTP配置控制器
 */
@Tag(name = "SMTP配置", description = "邮箱SMTP服务器配置管理")
@RestController
@RequestMapping("/api/v1/settings/smtp")
@RequiredArgsConstructor
public class SmtpConfigController {

    private final SmtpConfigService smtpConfigService;

    @Operation(summary = "获取SMTP配置列表")
    @GetMapping
    public Result<List<SmtpConfigVO>> getList() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<SmtpConfigVO> list = smtpConfigService.getList(userId);
        return Result.success(list);
    }

    @Operation(summary = "获取SMTP配置详情")
    @GetMapping("/{id}")
    public Result<SmtpConfigVO> getDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        SmtpConfigVO vo = smtpConfigService.getDetail(userId, id);
        return Result.success(vo);
    }

    @Operation(summary = "创建SMTP配置")
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody SmtpConfigRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long id = smtpConfigService.create(userId, request);
        return Result.success(Map.of("id", id.toString()));
    }

    @Operation(summary = "更新SMTP配置")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SmtpConfigRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        smtpConfigService.update(userId, id, request);
        return Result.success();
    }

    @Operation(summary = "删除SMTP配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        smtpConfigService.delete(userId, id);
        return Result.success();
    }

    @Operation(summary = "测试SMTP连接")
    @PostMapping("/{id}/test")
    public Result<Map<String, Object>> testConnection(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean success = smtpConfigService.testConnection(userId, id);
        return Result.success(Map.of(
                "success", success,
                "message", success ? "连接成功，测试邮件已发送" : "连接失败，请检查配置"
        ));
    }
}

