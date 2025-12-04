package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 报价导入项DTO
 */
@Data
@Schema(description = "报价导入项")
public class QuoteImportItemDTO {

    @Schema(description = "行号")
    private Integer rowIndex;

    @Schema(description = "OE编号")
    private String oeNo;

    @Schema(description = "客户报价(原始)")
    private String customerPriceRaw;

    @Schema(description = "客户报价(USD)")
    private BigDecimal customerPriceUsd;

    @Schema(description = "是否匹配到产品")
    private Boolean matched = false;

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "XK编号")
    private String xkNo;

    @Schema(description = "品牌缩写")
    private String brandCode;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "我方最低价(RMB)")
    private BigDecimal ourPriceMin;

    @Schema(description = "我方平均价(RMB)")
    private BigDecimal ourPriceAvg;

    @Schema(description = "我方最高价(RMB)")
    private BigDecimal ourPriceMax;

    @Schema(description = "我方选定价(RMB)")
    private BigDecimal ourPriceRmb;

    @Schema(description = "我方选定价(USD)")
    private BigDecimal ourPriceUsd;

    @Schema(description = "数量")
    private Integer quantity = 1;

    @Schema(description = "利润率(%)")
    private BigDecimal profitRate;

    @Schema(description = "是否含税")
    private Boolean includeTax = false;

    @Schema(description = "是否FOB")
    private Boolean isFob = false;

    @Schema(description = "计算后价格(USD)")
    private BigDecimal calculatedPrice;

    @Schema(description = "价格差异(我方计算价 - 客户报价)")
    private BigDecimal priceDiff;

    @Schema(description = "价格差异百分比")
    private BigDecimal priceDiffPercent;

    @Schema(description = "备注")
    private String remark;
}

