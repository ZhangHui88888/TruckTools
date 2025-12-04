package com.trucktools.customer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 客户导入记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_customer_import", autoResultMap = true)
@Schema(description = "客户导入记录")
public class CustomerImport extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "文件大小")
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

    @Schema(description = "导入模式: append, overwrite, merge")
    private String importMode;

    @Schema(description = "字段映射关系")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> fieldMapping;

    @Schema(description = "错误数据文件URL")
    private String errorFileUrl;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "开始处理时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
}

