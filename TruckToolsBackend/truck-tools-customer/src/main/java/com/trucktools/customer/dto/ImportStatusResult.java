package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入状态结果
 */
@Data
@Builder
@Schema(description = "导入状态结果")
public class ImportStatusResult {

    @Schema(description = "导入任务ID")
    private String importId;

    @Schema(description = "状态: pending, processing, completed, failed")
    private String status;

    @Schema(description = "总数据行数")
    private Integer totalRows;

    @Schema(description = "成功导入数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

    @Schema(description = "跳过数量（重复）")
    private Integer skippedCount;

    @Schema(description = "进度百分比")
    private Integer progress;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "导入日志下载URL")
    private String logFileUrl;
}

