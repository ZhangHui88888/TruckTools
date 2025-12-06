package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作台事件查询请求
 */
@Data
@Schema(description = "工作台事件查询请求")
public class WorkbenchEventQueryRequest {

    @Schema(description = "页码，从1开始")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;

    @Schema(description = "客户名称关键词")
    private String customerKeyword;

    @Schema(description = "事件状态: pending_customer, pending_us, all")
    private String eventStatus;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否只显示超时事件")
    private Boolean overdueOnly;

    @Schema(description = "是否排除已停止跟进的客户")
    private Boolean excludeStoppedFollowUp = true;
}
