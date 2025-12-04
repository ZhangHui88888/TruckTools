package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 名片视图对象
 */
@Data
@Schema(description = "名片信息")
public class BusinessCardVO {

    @Schema(description = "名片ID")
    private String id;

    @Schema(description = "名片图片URL")
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "OCR状态: 0=待识别, 1=识别中, 2=识别成功, 3=识别失败")
    private Integer ocrStatus;

    @Schema(description = "OCR置信度")
    private Double ocrConfidence;

    @Schema(description = "解析数据")
    private ParsedData parsedData;

    @Schema(description = "是否包含微信二维码")
    private Boolean hasWechatQr;

    @Schema(description = "是否包含WhatsApp二维码")
    private Boolean hasWhatsappQr;

    @Schema(description = "是否已处理")
    private Boolean isProcessed;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Data
    @Schema(description = "解析数据")
    public static class ParsedData {
        private String name;
        private String email;
        private String phone;
        private String company;
        private String position;
        private String address;
        private String website;
    }
}

