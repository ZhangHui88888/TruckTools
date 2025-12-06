package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工作台统计数据VO
 */
@Data
@Schema(description = "工作台统计数据")
public class WorkbenchStatsVO {

    @Schema(description = "待处理事件数量(等待我方处理)")
    private Integer pendingCount;

    @Schema(description = "等待反馈数量(等待客户反馈)")
    private Integer waitingCount;

    @Schema(description = "今日处理数量")
    private Integer todayProcessedCount;

    @Schema(description = "超时未跟进客户数量(超过3天)")
    private Integer overdueCount;

    @Schema(description = "本周新增客户数量")
    private Integer weeklyNewCustomerCount;

    @Schema(description = "活跃客户数量(近7天有事件更新)")
    private Integer activeCustomerCount;

    @Schema(description = "总客户数量")
    private Integer totalCustomerCount;

    @Schema(description = "已停止跟进客户数量")
    private Integer stoppedFollowUpCount;
}
