package com.trucktools.customer.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话术模板VO
 */
@Data
@Schema(description = "话术模板")
public class ScriptTemplateVO {

    @Schema(description = "模板ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "模板内容")
    private String content;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
