package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 停止跟进请求
 */
@Data
@Schema(description = "停止跟进请求")
public class StopFollowUpRequest {

    @NotNull(message = "客户ID不能为空")
    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "停止跟进原因")
    private String reason;
}
