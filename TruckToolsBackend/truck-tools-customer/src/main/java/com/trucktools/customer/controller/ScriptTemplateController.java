package com.trucktools.customer.controller;

import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.customer.dto.GeneratedScriptVO;
import com.trucktools.customer.dto.ScriptTemplateRequest;
import com.trucktools.customer.dto.ScriptTemplateVO;
import com.trucktools.customer.service.ScriptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 话术模板控制器
 */
@Tag(name = "话术模板管理")
@RestController
@RequestMapping("/api/v1/script-templates")
@RequiredArgsConstructor
public class ScriptTemplateController {

    private final ScriptTemplateService templateService;

    @Operation(summary = "创建话术模板")
    @PostMapping
    public Result<ScriptTemplateVO> create(@Valid @RequestBody ScriptTemplateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.create(userId, request));
    }

    @Operation(summary = "更新话术模板")
    @PutMapping("/{id}")
    public Result<ScriptTemplateVO> update(
            @PathVariable("id") Long templateId,
            @Valid @RequestBody ScriptTemplateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.update(userId, templateId, request));
    }

    @Operation(summary = "删除话术模板")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long templateId) {
        Long userId = SecurityUtils.getCurrentUserId();
        templateService.delete(userId, templateId);
        return Result.success();
    }

    @Operation(summary = "获取话术模板详情")
    @GetMapping("/{id}")
    public Result<ScriptTemplateVO> getDetail(@PathVariable("id") Long templateId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.getDetail(userId, templateId));
    }

    @Operation(summary = "获取话术模板列表")
    @GetMapping
    public Result<List<ScriptTemplateVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.listByUserId(userId));
    }

    @Operation(summary = "根据客户生成话术")
    @GetMapping("/generate/{customerId}")
    public Result<List<GeneratedScriptVO>> generateForCustomer(@PathVariable("customerId") Long customerId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.generateScriptsForCustomer(userId, customerId));
    }

    @Operation(summary = "获取支持的变量列表")
    @GetMapping("/variables")
    public Result<List<String>> getSupportedVariables() {
        return Result.success(templateService.getSupportedVariables());
    }
}
