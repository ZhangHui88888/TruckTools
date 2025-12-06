package com.trucktools.customer.controller;

import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.customer.dto.CustomerEventRequest;
import com.trucktools.customer.dto.CustomerEventVO;
import com.trucktools.customer.service.CustomerEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户事件控制器
 */
@Tag(name = "客户事件管理", description = "客户事件CRUD操作")
@RestController
@RequestMapping("/api/v1/customer-events")
@RequiredArgsConstructor
public class CustomerEventController {

    private final CustomerEventService eventService;

    @Operation(summary = "创建事件")
    @PostMapping
    public Result<CustomerEventVO> create(@Valid @RequestBody CustomerEventRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        CustomerEventVO vo = eventService.create(userId, request);
        return Result.success(vo);
    }

    @Operation(summary = "更新事件")
    @PutMapping("/{id}")
    public Result<CustomerEventVO> update(@PathVariable Long id, @Valid @RequestBody CustomerEventRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        CustomerEventVO vo = eventService.update(userId, id, request);
        return Result.success(vo);
    }

    @Operation(summary = "删除事件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        eventService.delete(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取客户的所有事件")
    @GetMapping("/customer/{customerId}")
    public Result<List<CustomerEventVO>> listByCustomerId(@PathVariable Long customerId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CustomerEventVO> events = eventService.listByCustomerId(userId, customerId);
        return Result.success(events);
    }

    @Operation(summary = "获取事件详情")
    @GetMapping("/{id}")
    public Result<CustomerEventVO> getDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        CustomerEventVO vo = eventService.getDetail(userId, id);
        return Result.success(vo);
    }
}

