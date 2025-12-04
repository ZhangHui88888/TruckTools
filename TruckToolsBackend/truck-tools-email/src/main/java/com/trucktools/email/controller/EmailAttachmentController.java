package com.trucktools.email.controller;

import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.email.dto.EmailAttachmentVO;
import com.trucktools.email.service.EmailAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 邮件附件控制器
 */
@Tag(name = "邮件附件", description = "邮件附件管理")
@RestController
@RequestMapping("/api/v1/email/attachments")
@RequiredArgsConstructor
public class EmailAttachmentController {

    private final EmailAttachmentService attachmentService;

    @Operation(summary = "上传附件")
    @PostMapping("/upload")
    public Result<EmailAttachmentVO> upload(
            @RequestParam(required = false) Long templateId,
            @RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        EmailAttachmentVO vo = attachmentService.upload(userId, templateId, file);
        return Result.success(vo);
    }

    @Operation(summary = "删除附件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        attachmentService.delete(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取模板的附件列表")
    @GetMapping("/template/{templateId}")
    public Result<List<EmailAttachmentVO>> getByTemplateId(@PathVariable Long templateId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<EmailAttachmentVO> attachments = attachmentService.getByTemplateId(userId, templateId);
        return Result.success(attachments);
    }
}

