package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 导入验证请求
 */
@Data
@Schema(description = "导入验证请求")
public class ImportValidateRequest {

    @Schema(description = "字段映射关系，key为Excel列名，value为系统字段名")
    private Map<String, String> fieldMapping;

    @Schema(description = "导入模式: append=追加, overwrite=覆盖, merge=合并")
    private String importMode;
}

