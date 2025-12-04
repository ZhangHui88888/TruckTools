package com.trucktools.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 邮件模板请求
 */
@Data
@Schema(description = "邮件模板请求")
public class EmailTemplateRequest {

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", required = true)
    private String name;

    @NotBlank(message = "邮件主题不能为空")
    @Schema(description = "邮件主题", required = true)
    private String subject;

    @NotBlank(message = "邮件正文不能为空")
    @Schema(description = "邮件正文", required = true)
    private String content;

    @Schema(description = "模板分类")
    private String category;
}

