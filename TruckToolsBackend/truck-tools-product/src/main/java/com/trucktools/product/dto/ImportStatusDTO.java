package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入状态DTO
 */
@Data
@Schema(description = "导入状态")
public class ImportStatusDTO {

    @Schema(description = "导入ID")
    private String importId;

    @Schema(description = "状态: pending/processing/completed/failed")
    private String status;

    @Schema(description = "总行数")
    private Integer totalRows;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

    @Schema(description = "跳过数量")
    private Integer skippedCount;

    @Schema(description = "进度(0-100)")
    private Integer progress;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "错误信息")
    private String errorMessage;
}

