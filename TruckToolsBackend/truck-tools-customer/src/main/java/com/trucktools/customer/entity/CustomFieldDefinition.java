package com.trucktools.customer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 自定义字段定义实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_custom_field_definition", autoResultMap = true)
@Schema(description = "自定义字段定义")
public class CustomFieldDefinition extends BaseEntity {

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "字段键名")
    private String fieldKey;

    @Schema(description = "字段显示名称")
    private String fieldName;

    @Schema(description = "字段类型: text, number, date, select")
    private String fieldType;

    @Schema(description = "选项值(select类型)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fieldOptions;

    @Schema(description = "是否必填")
    private Integer isRequired;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "状态: 0=禁用, 1=正常")
    private Integer status;
}

