package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户事件视图对象
 */
@Data
@Schema(description = "客户事件信息")
public class CustomerEventVO {

    @Schema(description = "事件ID")
    private String id;

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "事件地点")
    private String eventLocation;

    @Schema(description = "事件内容")
    private String eventContent;

    @Schema(description = "事件进度状态: pending_customer=等待客户回复, pending_us=等待我们回复")
    private String eventStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

