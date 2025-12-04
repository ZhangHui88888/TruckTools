package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Excel上传解析结果
 */
@Data
@Builder
@Schema(description = "Excel上传解析结果")
public class ImportUploadResult {

    @Schema(description = "导入任务ID")
    private String importId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "总数据行数（不含表头）")
    private Integer totalRows;

    @Schema(description = "Excel表头列表")
    private List<String> headers;

    @Schema(description = "系统建议的字段映射")
    private Map<String, String> suggestedMapping;

    @Schema(description = "数据预览（前几行）")
    private List<Map<String, String>> previewData;
}

