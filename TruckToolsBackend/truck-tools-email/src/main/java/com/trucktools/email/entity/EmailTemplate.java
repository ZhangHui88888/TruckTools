package com.trucktools.email.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_email_template", autoResultMap = true)
@Schema(description = "邮件模板")
public class EmailTemplate extends BaseEntity {

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "邮件正文(HTML)")
    private String content;

    @Schema(description = "纯文本版本")
    private String contentText;

    @Schema(description = "使用的变量列表")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> variables;

    @Schema(description = "模板分类")
    private String category;

    @Schema(description = "是否默认模板")
    private Integer isDefault;

    @Schema(description = "使用次数")
    private Integer useCount;

    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedAt;

    @Schema(description = "状态: 0=禁用, 1=正常")
    private Integer status;
}

