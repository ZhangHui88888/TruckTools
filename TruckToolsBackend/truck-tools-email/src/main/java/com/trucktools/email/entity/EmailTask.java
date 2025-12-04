package com.trucktools.email.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 邮件发送任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_email_task", autoResultMap = true)
@Schema(description = "邮件发送任务")
public class EmailTask extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "使用的模板ID")
    private Long templateId;

    @Schema(description = "SMTP配置ID")
    private Long smtpConfigId;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "邮件正文模板")
    private String content;

    @Schema(description = "客户筛选条件")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> filterConditions;

    @Schema(description = "总发送数量")
    private Integer totalCount;

    @Schema(description = "已发送数量")
    private Integer sentCount;

    @Schema(description = "发送成功数量")
    private Integer successCount;

    @Schema(description = "发送失败数量")
    private Integer failedCount;

    @Schema(description = "状态: 0=待发送, 1=发送中, 2=已暂停, 3=已完成, 4=已取消")
    private Integer status;

    @Schema(description = "计划发送时间")
    private LocalDateTime scheduledAt;

    @Schema(description = "开始发送时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "错误信息")
    private String errorMessage;
}

