package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 自定义字段VO
 */
@Data
@Schema(description = "自定义字段VO")
public class CustomFieldVO {

    @Schema(description = "字段ID")
    private String id;

    @Schema(description = "字段键名")
    private String fieldKey;

    @Schema(description = "显示名称")
    private String fieldName;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "选项值")
    private List<String> fieldOptions;

    @Schema(description = "是否必填")
    private Boolean isRequired;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "排序")
    private Integer sortOrder;
}

