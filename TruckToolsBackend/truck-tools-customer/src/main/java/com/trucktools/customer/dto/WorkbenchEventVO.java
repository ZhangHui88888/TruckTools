package com.trucktools.customer.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作台事件VO
 */
@Data
@Schema(description = "工作台事件")
public class WorkbenchEventVO {

    @Schema(description = "事件ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "客户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long customerId;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "所属公司")
    private String customerCompany;

    @Schema(description = "客户优先级")
    private Integer customerPriority;

    @Schema(description = "客户备注")
    private String customerRemark;

    @Schema(description = "客户邮箱")
    private String customerEmail;

    @Schema(description = "客户电话")
    private String customerPhone;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "事件地点")
    private String eventLocation;

    @Schema(description = "事件内容")
    private String eventContent;

    @Schema(description = "事件状态: pending_customer=等待客户反馈, pending_us=等待我方处理")
    private String eventStatus;

    @Schema(description = "是否系统自动生成")
    private Boolean isSystemGenerated;

    @Schema(description = "等待天数")
    private Integer waitingDays;

    @Schema(description = "是否超时(超过3天)")
    private Boolean isOverdue;

    @Schema(description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
