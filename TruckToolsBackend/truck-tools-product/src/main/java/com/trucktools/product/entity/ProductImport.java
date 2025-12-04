package com.trucktools.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 产品导入记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_import")
@Schema(description = "产品导入记录")
public class ProductImport extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "总行数")
    private Integer totalRows;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

    @Schema(description = "跳过数量")
    private Integer skippedCount;

    @Schema(description = "状态: 0=待处理, 1=处理中, 2=已完成, 3=失败")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "开始处理时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
}

