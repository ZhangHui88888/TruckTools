package com.trucktools.email.controller;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.email.dto.CreateTaskRequest;
import com.trucktools.email.dto.EmailLogVO;
import com.trucktools.email.dto.EmailTaskVO;
import com.trucktools.email.service.EmailTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 邮件任务控制器
 */
@Tag(name = "邮件任务", description = "邮件发送任务管理")
@RestController
@RequestMapping("/api/v1/email/tasks")
@RequiredArgsConstructor
public class EmailTaskController {

    private final EmailTaskService taskService;

    @Operation(summary = "创建发送任务")
    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody CreateTaskRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = taskService.create(userId, request);
        return Result.success("发送任务已创建", result);
    }

    @Operation(summary = "获取任务列表")
    @GetMapping
    public Result<PageResult<EmailTaskVO>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResult<EmailTaskVO> result = taskService.getPage(userId, page, pageSize, status);
        return Result.success(result);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{taskId}")
    public Result<EmailTaskVO> getDetail(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        EmailTaskVO vo = taskService.getDetail(userId, taskId);
        return Result.success(vo);
    }

    @Operation(summary = "开始发送")
    @PostMapping("/{taskId}/start")
    public Result<Void> start(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        taskService.start(userId, taskId);
        return Result.success();
    }

    @Operation(summary = "暂停发送")
    @PostMapping("/{taskId}/pause")
    public Result<Void> pause(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        taskService.pause(userId, taskId);
        return Result.success();
    }

    @Operation(summary = "继续发送")
    @PostMapping("/{taskId}/resume")
    public Result<Void> resume(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        taskService.resume(userId, taskId);
        return Result.success();
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{taskId}/cancel")
    public Result<Void> cancel(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        taskService.cancel(userId, taskId);
        return Result.success();
    }

    @Operation(summary = "获取发送日志")
    @GetMapping("/{taskId}/logs")
    public Result<PageResult<EmailLogVO>> getLogs(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResult<EmailLogVO> result = taskService.getLogs(userId, taskId, page, pageSize, status);
        return Result.success(result);
    }

    @Operation(summary = "重试失败的邮件")
    @PostMapping("/{taskId}/retry")
    public Result<Void> retry(@PathVariable Long taskId, @RequestBody(required = false) Map<String, List<Long>> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> logIds = request != null ? request.get("logIds") : null;
        taskService.retry(userId, taskId, logIds);
        return Result.success();
    }

    @Operation(summary = "导出发送日志")
    @PostMapping("/{taskId}/export")
    public Result<Map<String, String>> exportLogs(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        String downloadUrl = taskService.exportLogs(userId, taskId);
        return Result.success(Map.of(
                "downloadUrl", downloadUrl,
                "expiresAt", LocalDateTime.now().plusDays(1).toString()
        ));
    }
}

