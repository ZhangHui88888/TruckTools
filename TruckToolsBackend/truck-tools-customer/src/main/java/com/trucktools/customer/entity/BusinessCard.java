package com.trucktools.customer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 名片识别记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_business_card", autoResultMap = true)
@Schema(description = "名片识别记录")
public class BusinessCard extends BaseEntity {

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "关联客户ID")
    private Long customerId;

    @Schema(description = "名片图片URL")
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String imageThumbnail;

    @Schema(description = "优先级: 1=高, 2=中, 3=低")
    private Integer priority;

    @Schema(description = "OCR状态: 0=待识别, 1=识别中, 2=识别成功, 3=识别失败")
    private Integer ocrStatus;

    @Schema(description = "OCR原始识别结果")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ocrResult;

    @Schema(description = "OCR置信度")
    private BigDecimal ocrConfidence;

    @Schema(description = "OCR服务商")
    private String ocrProvider;

    @Schema(description = "OCR识别时间")
    private LocalDateTime ocrTime;

    @Schema(description = "OCR错误信息")
    private String ocrError;

    @Schema(description = "解析-姓名")
    private String parsedName;

    @Schema(description = "解析-邮箱")
    private String parsedEmail;

    @Schema(description = "解析-电话")
    private String parsedPhone;

    @Schema(description = "解析-公司")
    private String parsedCompany;

    @Schema(description = "解析-职位")
    private String parsedPosition;

    @Schema(description = "解析-地址")
    private String parsedAddress;

    @Schema(description = "解析-网站")
    private String parsedWebsite;

    @Schema(description = "是否包含微信二维码")
    private Integer hasWechatQr;

    @Schema(description = "是否包含WhatsApp二维码")
    private Integer hasWhatsappQr;

    @Schema(description = "是否已处理")
    private Integer isProcessed;

    @Schema(description = "上传批次号")
    private String uploadBatch;
}

