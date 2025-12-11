package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 延迟提醒请求
 */
@Data
@Schema(description = "延迟提醒请求")
public class SnoozeReminderRequest {

    @NotNull(message = "事件ID不能为空")
    @Schema(description = "要延迟的事件ID")
    private Long eventId;

    @NotNull(message = "延迟天数不能为空")
    @Min(value = 1, message = "延迟天数至少为1天")
    @Schema(description = "延迟天数", example = "7")
    private Integer snoozeDays;
}
