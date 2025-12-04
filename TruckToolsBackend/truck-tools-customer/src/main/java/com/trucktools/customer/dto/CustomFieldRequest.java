package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 自定义字段请求DTO
 */
@Data
@Schema(description = "自定义字段请求")
public class CustomFieldRequest {

    @NotBlank(message = "字段键名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "只能包含字母、数字和下划线，且以字母开头")
    @Schema(description = "字段键名")
    private String fieldKey;

    @NotBlank(message = "显示名称不能为空")
    @Schema(description = "显示名称")
    private String fieldName;

    @Schema(description = "字段类型: text, number, date, select")
    private String fieldType = "text";

    @Schema(description = "选项值(select类型)")
    private List<String> fieldOptions;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "是否必填")
    private Boolean isRequired = false;
}

