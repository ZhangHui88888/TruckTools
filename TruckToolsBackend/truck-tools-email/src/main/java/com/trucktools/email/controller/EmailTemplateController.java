package com.trucktools.email.controller;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.email.dto.EmailTemplateRequest;
import com.trucktools.email.dto.EmailTemplateVO;
import com.trucktools.email.service.EmailTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 邮件模板控制器
 */
@Tag(name = "邮件模板", description = "邮件模板管理")
@RestController
@RequestMapping("/api/v1/email/templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService templateService;

    @Operation(summary = "获取模板列表")
    @GetMapping
    public Result<PageResult<EmailTemplateVO>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResult<EmailTemplateVO> result = templateService.getPage(userId, page, pageSize, category);
        return Result.success(result);
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<EmailTemplateVO> getDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        EmailTemplateVO vo = templateService.getDetail(userId, id);
        return Result.success(vo);
    }

    @Operation(summary = "创建模板")
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody EmailTemplateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long id = templateService.create(userId, request);
        return Result.success(Map.of("id", id.toString()));
    }

    @Operation(summary = "更新模板")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EmailTemplateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        templateService.update(userId, id, request);
        return Result.success();
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        templateService.delete(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取可用变量列表")
    @GetMapping("/variables")
    public Result<Map<String, List<Map<String, String>>>> getVariables() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, List<Map<String, String>>> variables = templateService.getVariables(userId);
        return Result.success(variables);
    }

    @Operation(summary = "预览邮件")
    @PostMapping("/preview")
    public Result<Map<String, Object>> preview(@RequestBody Map<String, Object> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String subject = (String) request.get("subject");
        String content = (String) request.get("content");
        Long customerId = Long.valueOf(request.get("customerId").toString());
        
        Map<String, Object> result = templateService.preview(userId, subject, content, customerId);
        return Result.success(result);
    }
}

