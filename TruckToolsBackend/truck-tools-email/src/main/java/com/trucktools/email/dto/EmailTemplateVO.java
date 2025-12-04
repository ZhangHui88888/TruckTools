package com.trucktools.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件模板视图对象
 */
@Data
@Schema(description = "邮件模板")
public class EmailTemplateVO {

    @Schema(description = "模板ID")
    private String id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "邮件正文")
    private String content;

    @Schema(description = "纯文本版本")
    private String contentText;

    @Schema(description = "使用的变量列表")
    private List<String> variables;

    @Schema(description = "模板分类")
    private String category;

    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    @Schema(description = "使用次数")
    private Integer useCount;

    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

