package com.trucktools.email.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮件附件实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_email_attachment")
@Schema(description = "邮件附件")
public class EmailAttachment extends BaseEntity {

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "关联模板ID")
    private Long templateId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "存储路径")
    private String filePath;

    @Schema(description = "访问URL")
    private String fileUrl;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件MIME类型")
    private String fileType;

    @Schema(description = "文件扩展名")
    private String fileExtension;

    @Schema(description = "状态: 0=禁用, 1=正常")
    private Integer status;
}

