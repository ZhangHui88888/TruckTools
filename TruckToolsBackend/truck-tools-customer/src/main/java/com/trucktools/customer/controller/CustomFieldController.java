package com.trucktools.customer.controller;

import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.customer.dto.CustomFieldRequest;
import com.trucktools.customer.dto.CustomFieldVO;
import com.trucktools.customer.service.CustomFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 自定义字段控制器
 */
@Tag(name = "自定义字段", description = "客户自定义字段管理")
@RestController
@RequestMapping("/api/v1/custom-fields")
@RequiredArgsConstructor
public class CustomFieldController {

    private final CustomFieldService customFieldService;

    @Operation(summary = "获取自定义字段列表")
    @GetMapping
    public Result<List<CustomFieldVO>> getList() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CustomFieldVO> list = customFieldService.getList(userId);
        return Result.success(list);
    }

    @Operation(summary = "创建自定义字段")
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody CustomFieldRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long id = customFieldService.create(userId, request);
        return Result.success(Map.of("id", id.toString()));
    }

    @Operation(summary = "更新自定义字段")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CustomFieldRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        customFieldService.update(userId, id, request);
        return Result.success();
    }

    @Operation(summary = "删除自定义字段")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        customFieldService.delete(userId, id);
        return Result.success();
    }
}

