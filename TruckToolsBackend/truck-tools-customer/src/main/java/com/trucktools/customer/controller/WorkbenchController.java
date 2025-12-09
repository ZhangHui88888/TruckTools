package com.trucktools.customer.controller;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.customer.dto.*;
import com.trucktools.customer.service.WorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 工作台控制器
 */
@Tag(name = "工作台管理")
@RestController
@RequestMapping("/api/v1/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @Operation(summary = "获取工作台统计数据")
    @GetMapping("/stats")
    public Result<WorkbenchStatsVO> getStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        // 每次获取统计数据时，自动检查并创建超时提醒
        workbenchService.checkAndCreateOverdueReminders();
        return Result.success(workbenchService.getStats(userId));
    }

    @Operation(summary = "获取工作台事件列表")
    @GetMapping("/events")
    public Result<PageResult<WorkbenchEventVO>> getEventList(WorkbenchEventQueryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(workbenchService.getEventList(userId, request));
    }

    @Operation(summary = "处理事件")
    @PostMapping("/events/process")
    public Result<WorkbenchEventVO> processEvent(@Valid @RequestBody ProcessEventRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(workbenchService.processEvent(userId, request));
    }

    @Operation(summary = "停止跟进客户")
    @PostMapping("/customers/stop-follow-up")
    public Result<Void> stopFollowUp(@Valid @RequestBody StopFollowUpRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        workbenchService.stopFollowUp(userId, request);
        return Result.success();
    }

    @Operation(summary = "恢复跟进客户")
    @PostMapping("/customers/{customerId}/resume-follow-up")
    public Result<Void> resumeFollowUp(@PathVariable Long customerId) {
        Long userId = SecurityUtils.getCurrentUserId();
        workbenchService.resumeFollowUp(userId, customerId);
        return Result.success();
    }

    @Operation(summary = "手动触发超时提醒检查（仅管理员）")
    @PostMapping("/check-overdue")
    public Result<Integer> checkOverdue() {
        int count = workbenchService.checkAndCreateOverdueReminders();
        return Result.success(count);
    }

    @Operation(summary = "导出待处理事件列表")
    @GetMapping("/events/export")
    public void exportEvents(WorkbenchEventQueryRequest request, HttpServletResponse response) {
        Long userId = SecurityUtils.getCurrentUserId();
        workbenchService.exportEvents(userId, request, response);
    }
}
