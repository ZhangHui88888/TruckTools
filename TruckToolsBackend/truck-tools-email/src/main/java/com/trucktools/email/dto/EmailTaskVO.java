package com.trucktools.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件任务视图对象
 */
@Data
@Schema(description = "邮件发送任务")
public class EmailTaskVO {

    @Schema(description = "任务ID")
    private String id;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "SMTP配置ID")
    private String smtpConfigId;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "邮件正文")
    private String content;

    @Schema(description = "总发送数量")
    private Integer totalCount;

    @Schema(description = "已发送数量")
    private Integer sentCount;

    @Schema(description = "发送成功数量")
    private Integer successCount;

    @Schema(description = "发送失败数量")
    private Integer failedCount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "进度百分比")
    private Double progress;

    @Schema(description = "计划发送时间")
    private LocalDateTime scheduledAt;

    @Schema(description = "开始发送时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "预计完成时间")
    private LocalDateTime estimatedEndTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}

