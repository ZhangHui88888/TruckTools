package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 客户视图对象
 */
@Data
@Schema(description = "客户信息")
public class CustomerVO {

    @Schema(description = "客户ID")
    private String id;

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

    @Schema(description = "优先级")
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

    @Schema(description = "名片正面图片URL")
    private String businessCardFront;

    @Schema(description = "名片背面图片URL")
    private String businessCardBack;

    @Schema(description = "跟进状态: pending_customer=等待客户回复, pending_us=等待我们回复, completed=已完成")
    private String followUpStatus;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "来源文件")
    private String sourceFile;

    @Schema(description = "OCR置信度")
    private Double ocrConfidence;

    @Schema(description = "邮箱状态")
    private Integer emailStatus;

    @Schema(description = "最后发送邮件时间")
    private LocalDateTime lastEmailTime;

    @Schema(description = "已发送邮件数量")
    private Integer emailCount;

    @Schema(description = "自定义字段")
    private Map<String, String> customFields;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "最新事件内容")
    private String latestEventContent;

    @Schema(description = "最新事件时间")
    private LocalDateTime latestEventTime;

    @Schema(description = "最新事件类型")
    private String latestEventType;
}

