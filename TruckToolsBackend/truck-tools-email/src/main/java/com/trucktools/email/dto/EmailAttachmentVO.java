package com.trucktools.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件附件VO
 */
@Data
@Schema(description = "邮件附件VO")
public class EmailAttachmentVO {

    @Schema(description = "附件ID")
    private String id;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "访问URL")
    private String fileUrl;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件扩展名")
    private String fileExtension;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}

