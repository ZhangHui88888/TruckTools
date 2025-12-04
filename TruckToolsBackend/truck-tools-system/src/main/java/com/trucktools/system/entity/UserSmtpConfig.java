package com.trucktools.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户SMTP配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_smtp_config")
@Schema(description = "用户SMTP配置")
public class UserSmtpConfig extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "SMTP服务器地址")
    private String smtpHost;

    @Schema(description = "SMTP端口")
    private Integer smtpPort;

    @Schema(description = "SMTP用户名")
    private String smtpUsername;

    @Schema(description = "SMTP密码(加密存储)")
    private String smtpPassword;

    @Schema(description = "发件人显示名称")
    private String senderName;

    @Schema(description = "发件人邮箱")
    private String senderEmail;

    @Schema(description = "是否使用SSL")
    private Integer useSsl;

    @Schema(description = "是否使用TLS")
    private Integer useTls;

    @Schema(description = "是否默认配置")
    private Integer isDefault;

    @Schema(description = "状态: 0=禁用, 1=正常")
    private Integer status;

    @Schema(description = "测试状态: 0=失败, 1=成功")
    private Integer testStatus;

    @Schema(description = "最后测试时间")
    private LocalDateTime testTime;

    @Schema(description = "每日发送限制")
    private Integer dailyLimit;

    @Schema(description = "每小时发送限制")
    private Integer hourlyLimit;
}

