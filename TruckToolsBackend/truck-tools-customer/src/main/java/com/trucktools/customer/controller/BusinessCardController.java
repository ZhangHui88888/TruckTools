package com.trucktools.customer.controller;

import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.customer.dto.BusinessCardVO;
import com.trucktools.customer.dto.CustomerRequest;
import com.trucktools.customer.service.BusinessCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 名片控制器
 */
@Tag(name = "名片识别", description = "名片上传与OCR识别")
@RestController
@RequestMapping("/api/v1/business-cards")
@RequiredArgsConstructor
public class BusinessCardController {

    private final BusinessCardService businessCardService;

    @Operation(summary = "上传名片")
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "priorities", required = false) List<Integer> priorities) {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = businessCardService.upload(userId, files, priorities);
        return Result.success("上传成功，正在识别", result);
    }

    @Operation(summary = "获取识别结果")
    @GetMapping("/{id}")
    public Result<BusinessCardVO> getResult(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        BusinessCardVO vo = businessCardService.getResult(userId, id);
        return Result.success(vo);
    }

    @Operation(summary = "查询批次识别状态")
    @GetMapping("/batch/{batchId}/status")
    public Result<Map<String, Object>> getBatchStatus(@PathVariable String batchId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = businessCardService.getBatchStatus(userId, batchId);
        return Result.success(result);
    }

    @Operation(summary = "确认识别结果转为客户")
    @PostMapping("/{id}/confirm")
    public Result<Map<String, String>> confirm(
            @PathVariable Long id, 
            @Valid @RequestBody CustomerRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long customerId = businessCardService.confirm(userId, id, request);
        return Result.success("客户创建成功", Map.of("customerId", customerId.toString()));
    }

    @Operation(summary = "批量确认转为客户")
    @PostMapping("/batch-confirm")
    public Result<Map<String, Integer>> batchConfirm(@RequestBody Map<String, List<Long>> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> cardIds = request.get("cardIds");
        Map<String, Integer> result = businessCardService.batchConfirm(userId, cardIds);
        return Result.success(result);
    }
}

