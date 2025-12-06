package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 客户事件请求对象
 */
@Data
@Schema(description = "客户事件请求")
public class CustomerEventRequest {

    @Schema(description = "客户ID")
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @Schema(description = "事件时间（格式：YYYY-MM-DD）")
    @NotBlank(message = "事件时间不能为空")
    private String eventTime;

    @Schema(description = "事件地点")
    private String eventLocation;

    @Schema(description = "事件内容")
    @NotBlank(message = "事件内容不能为空")
    private String eventContent;

    @Schema(description = "事件进度状态: pending_customer=等待客户回复, pending_us=等待我们回复")
    @NotBlank(message = "事件状态不能为空")
    private String eventStatus;
}

