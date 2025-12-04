package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品VO
 */
@Data
@Schema(description = "产品信息")
public class ProductVO {

    @Schema(description = "产品ID")
    private String id;

    @Schema(description = "品牌缩写")
    private String brandCode;

    @Schema(description = "品牌全称")
    private String brandName;

    @Schema(description = "内部编号")
    private String xkNo;

    @Schema(description = "OE编号")
    private String oeNo;

    @Schema(description = "图片路径")
    private String imagePath;

    @Schema(description = "图片完整URL")
    private String imageUrl;

    @Schema(description = "最低价(RMB)")
    private BigDecimal priceMin;

    @Schema(description = "最高价(RMB)")
    private BigDecimal priceMax;

    @Schema(description = "平均价(RMB)")
    private BigDecimal priceAvg;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

