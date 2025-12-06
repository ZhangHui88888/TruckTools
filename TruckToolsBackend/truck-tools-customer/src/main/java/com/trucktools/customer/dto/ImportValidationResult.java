package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 导入数据验证结果
 */
@Data
@Builder
@Schema(description = "导入数据验证结果")
public class ImportValidationResult {

    @Schema(description = "总数据行数")
    private Integer totalRows;

    @Schema(description = "有效数据行数")
    private Integer validRows;

    @Schema(description = "无效数据行数")
    private Integer invalidRows;

    @Schema(description = "重复数据行数（文件内重复 + 数据库重复）")
    private Integer duplicateRows;

    @Schema(description = "文件内重复行数（将被跳过）")
    private Integer duplicateInFile;

    @Schema(description = "数据库重复行数（根据导入模式处理）")
    private Integer duplicateInDb;

    @Schema(description = "错误详情列表")
    private List<ImportError> errors;

    @Schema(description = "错误报告下载URL")
    private String errorFileUrl;

    @Schema(description = "可导入数据预览（新数据）")
    private List<Map<String, String>> newDataPreview;

    @Schema(description = "可更新数据预览（重复数据）")
    private List<Map<String, String>> duplicateDataPreview;

    @Data
    @Builder
    @Schema(description = "导入错误详情")
    public static class ImportError {
        @Schema(description = "行号")
        private Integer row;

        @Schema(description = "字段名")
        private String field;

        @Schema(description = "错误值")
        private String value;

        @Schema(description = "错误信息")
        private String message;
    }
}

