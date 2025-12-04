package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 报价项DTO
 */
@Data
@Schema(description = "报价项")
public class QuoteItemDTO {

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "XK编号")
    private String xkNo;

    @Schema(description = "OE编号")
    private String oeNo;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "品牌缩写")
    private String brandCode;

    @Schema(description = "出厂价(RMB)")
    private BigDecimal priceRmb;

    @Schema(description = "出厂价(USD)")
    private BigDecimal priceUsd;

    @Schema(description = "数量")
    private Integer quantity = 1;

    @Schema(description = "利润率(%)")
    private BigDecimal profitRate;

    @Schema(description = "是否含税")
    private Boolean includeTax = false;

    @Schema(description = "是否FOB")
    private Boolean isFob = false;

    @Schema(description = "推荐单价(USD)")
    private BigDecimal recommendedPrice;

    @Schema(description = "最终单价(USD)")
    private BigDecimal finalPrice;

    @Schema(description = "小计(USD)")
    private BigDecimal subtotal;

    @Schema(description = "备注")
    private String remark;
}

