package com.trucktools.customer.controller;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.core.domain.Result;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.customer.dto.CustomerQueryParam;
import com.trucktools.customer.dto.CustomerRequest;
import com.trucktools.customer.dto.CustomerVO;
import com.trucktools.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 客户控制器
 */
@Tag(name = "客户管理", description = "客户信息CRUD操作")
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "获取客户列表")
    @GetMapping
    public Result<PageResult<CustomerVO>> getList(CustomerQueryParam param) {
        param.setUserId(SecurityUtils.getCurrentUserId());
        PageResult<CustomerVO> result = customerService.getPage(param);
        return Result.success(result);
    }

    @Operation(summary = "获取客户详情")
    @GetMapping("/{id}")
    public Result<CustomerVO> getDetail(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.fail(ResultCode.PARAM_ERROR, "无效的客户ID");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        CustomerVO vo = customerService.getDetail(userId, id);
        return Result.success(vo);
    }

    @Operation(summary = "创建客户")
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody CustomerRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long id = customerService.create(userId, request);
        return Result.success(Map.of("id", id.toString()));
    }

    @Operation(summary = "更新客户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        customerService.update(userId, id, request);
        return Result.success();
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        customerService.delete(userId, id);
        return Result.success();
    }

    @Operation(summary = "批量删除客户")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody Map<String, List<Long>> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> ids = request.get("ids");
        customerService.batchDelete(userId, ids);
        return Result.success();
    }

    @Operation(summary = "导出客户")
    @PostMapping("/export")
    public Result<Map<String, String>> export(@RequestBody Map<String, Object> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        @SuppressWarnings("unchecked")
        Map<String, Object> filter = (Map<String, Object>) request.get("filter");
        @SuppressWarnings("unchecked")
        List<String> fields = (List<String>) request.get("fields");
        
        String downloadUrl = customerService.export(userId, ids, filter, fields);
        return Result.success(Map.of(
                "downloadUrl", downloadUrl,
                "expiresAt", java.time.LocalDateTime.now().plusDays(1).toString()
        ));
    }
}

