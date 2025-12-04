package com.trucktools.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 邮件日志视图对象
 */
@Data
@Schema(description = "邮件发送日志")
public class EmailLogVO {

    @Schema(description = "日志ID")
    private String id;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "客户ID")
    private String customerId;

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
    private Map<String, String> variablesData;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "错误代码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "发送时间")
    private LocalDateTime sentAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}

