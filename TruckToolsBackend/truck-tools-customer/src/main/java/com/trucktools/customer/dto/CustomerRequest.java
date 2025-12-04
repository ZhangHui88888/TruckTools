package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 客户请求对象
 */
@Data
@Schema(description = "客户请求")
public class CustomerRequest {

    @NotBlank(message = "客户姓名不能为空")
    @Schema(description = "客户姓名", required = true)
    private String name;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱（可选，如填写需符合邮箱格式）", required = false)
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "所属公司")
    private String company;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "公司官网")
    private String website;

    @Min(value = 1, message = "优先级范围为1-3")
    @Max(value = 3, message = "优先级范围为1-3")
    @Schema(description = "优先级: 1=高, 2=中, 3=低")
    private Integer priority = 2;

    @Schema(description = "会面时间（日期格式：YYYY-MM-DD）")
    private String meetingTime;

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

    @Schema(description = "自定义字段")
    private Map<String, String> customFields;
}

