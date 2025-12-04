package com.trucktools.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * SMTP配置请求
 */
@Data
@Schema(description = "SMTP配置请求")
public class SmtpConfigRequest {

    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称", required = true)
    private String configName;

    @NotBlank(message = "SMTP服务器地址不能为空")
    @Schema(description = "SMTP服务器地址", required = true)
    private String smtpHost;

    @NotNull(message = "SMTP端口不能为空")
    @Schema(description = "SMTP端口", required = true)
    private Integer smtpPort;

    @NotBlank(message = "SMTP用户名不能为空")
    @Schema(description = "SMTP用户名", required = true)
    private String smtpUsername;

    @NotBlank(message = "SMTP密码不能为空")
    @Schema(description = "SMTP密码", required = true)
    private String smtpPassword;

    @Schema(description = "发件人显示名称")
    private String senderName;

    @NotBlank(message = "发件人邮箱不能为空")
    @Email(message = "发件人邮箱格式不正确")
    @Schema(description = "发件人邮箱", required = true)
    private String senderEmail;

    @Schema(description = "是否使用SSL", defaultValue = "true")
    private Boolean useSsl = true;

    @Schema(description = "是否使用TLS", defaultValue = "false")
    private Boolean useTls = false;

    @Schema(description = "是否设为默认配置", defaultValue = "false")
    private Boolean isDefault = false;

    @Schema(description = "每日发送限制", defaultValue = "500")
    private Integer dailyLimit = 500;

    @Schema(description = "每小时发送限制", defaultValue = "100")
    private Integer hourlyLimit = 100;
}

