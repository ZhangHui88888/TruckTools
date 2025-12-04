package com.trucktools.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 创建发送任务请求
 */
@Data
@Schema(description = "创建发送任务请求")
public class CreateTaskRequest {

    @Schema(description = "任务名称")
    private String taskName;

    @NotNull(message = "模板ID不能为空")
    @Schema(description = "模板ID", required = true)
    private Long templateId;

    @NotNull(message = "SMTP配置ID不能为空")
    @Schema(description = "SMTP配置ID", required = true)
    private Long smtpConfigId;

    @Schema(description = "客户ID列表（指定发送）")
    private List<Long> customerIds;

    @Schema(description = "客户筛选条件（条件发送）")
    private Map<String, Object> filter;

    @Schema(description = "计划发送时间（定时发送）")
    private LocalDateTime scheduledAt;
}

