package com.trucktools.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 话术模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_script_template")
@Schema(description = "话术模板")
public class ScriptTemplate extends BaseEntity {

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "模板内容，支持变量如 {{name}}, {{company}} 等")
    private String content;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否启用: 0=禁用, 1=启用")
    private Integer enabled;
}
