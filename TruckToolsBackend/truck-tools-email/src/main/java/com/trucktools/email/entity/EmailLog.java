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
 * 邮件发送日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_email_log", autoResultMap = true)
@Schema(description = "邮件发送日志")
public class EmailLog extends BaseEntity {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "收件邮箱")
    private String customerEmail;

    @Schema(description = "客户公司")
    private String customerCompany;

    @Schema(description = "客户国家")
    private String customerCountry;

    @Schema(description = "客户优先级")
    private Integer customerPriority;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "邮件正文")
    private String content;

    @Schema(description = "变量替换数据")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> variablesData;

    @Schema(description = "状态: 0=待发送, 1=发送中, 2=发送成功, 3=发送失败")
    private Integer status;

    @Schema(description = "错误代码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "发送时间")
    private LocalDateTime sentAt;
}

