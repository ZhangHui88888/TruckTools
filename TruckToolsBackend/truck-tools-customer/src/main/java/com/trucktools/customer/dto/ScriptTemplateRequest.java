package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 话术模板请求DTO
 */
@Data
@Schema(description = "话术模板请求")
public class ScriptTemplateRequest {

    @Schema(description = "模板名称")
    @NotBlank(message = "模板名称不能为空")
    private String name;

    @Schema(description = "模板内容，支持变量如 {{name}}, {{company}} 等")
    @NotBlank(message = "模板内容不能为空")
    private String content;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
