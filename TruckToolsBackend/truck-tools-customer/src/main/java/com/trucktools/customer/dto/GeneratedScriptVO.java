package com.trucktools.customer.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 生成的话术VO（用于客户详情页展示）
 */
@Data
@Schema(description = "生成的话术")
public class GeneratedScriptVO {

    @Schema(description = "模板ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "生成的话术内容（已替换变量）")
    private String generatedContent;
}
