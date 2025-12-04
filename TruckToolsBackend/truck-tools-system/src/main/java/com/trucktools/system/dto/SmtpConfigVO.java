package com.trucktools.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SMTP配置视图对象
 */
@Data
@Schema(description = "SMTP配置信息")
public class SmtpConfigVO {

    @Schema(description = "配置ID")
    private String id;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "SMTP服务器地址")
    private String smtpHost;

    @Schema(description = "SMTP端口")
    private Integer smtpPort;

    @Schema(description = "SMTP用户名")
    private String smtpUsername;

    @Schema(description = "发件人显示名称")
    private String senderName;

    @Schema(description = "发件人邮箱")
    private String senderEmail;

    @Schema(description = "是否使用SSL")
    private Boolean useSsl;

    @Schema(description = "是否使用TLS")
    private Boolean useTls;

    @Schema(description = "是否默认配置")
    private Boolean isDefault;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "测试状态")
    private Integer testStatus;

    @Schema(description = "最后测试时间")
    private LocalDateTime testTime;

    @Schema(description = "每日发送限制")
    private Integer dailyLimit;

    @Schema(description = "每小时发送限制")
    private Integer hourlyLimit;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}

