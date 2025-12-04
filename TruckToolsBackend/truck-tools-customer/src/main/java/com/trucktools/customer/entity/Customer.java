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
 * 客户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_customer", autoResultMap = true)
@Schema(description = "客户")
public class Customer extends BaseEntity {

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "客户姓名")
    private String name;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "所属公司")
    private String company;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "国家代码")
    private String countryCode;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "公司官网")
    private String website;

    @Schema(description = "优先级: 1=高, 2=中, 3=低")
    private Integer priority;

    @Schema(description = "会面时间")
    private LocalDateTime meetingTime;

    @Schema(description = "会面地点")
    private String meetingLocation;

    @Schema(description = "微信名称/ID")
    private String wechatName;

    @Schema(description = "微信二维码图片URL")
    private String wechatQrcode;

    @Schema(description = "WhatsApp名称/号码")
    private String whatsappName;

    @Schema(description = "WhatsApp二维码图片URL")
    private String whatsappQrcode;

    @Schema(description = "跟进状态: pending_customer=等待客户回复, pending_us=等待我们回复, completed=已完成")
    private String followUpStatus;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "来源: manual, ocr, import")
    private String source;

    @Schema(description = "来源文件")
    private String sourceFile;

    @Schema(description = "OCR识别置信度")
    private BigDecimal ocrConfidence;

    @Schema(description = "邮箱状态: 0=无效, 1=有效, 2=退信")
    private Integer emailStatus;

    @Schema(description = "最后发送邮件时间")
    private LocalDateTime lastEmailTime;

    @Schema(description = "已发送邮件数量")
    private Integer emailCount;

    @Schema(description = "扩展数据")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extraData;
}

