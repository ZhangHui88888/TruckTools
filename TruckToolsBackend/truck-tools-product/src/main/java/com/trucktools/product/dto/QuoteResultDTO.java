package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报价结果DTO
 */
@Data
@Schema(description = "报价结果")
public class QuoteResultDTO {

    @Schema(description = "报价项列表")
    private List<QuoteItemDTO> items;

    @Schema(description = "产品总数")
    private Integer totalCount;

    @Schema(description = "总金额(USD)")
    private BigDecimal totalAmount;

    @Schema(description = "使用的汇率")
    private BigDecimal exchangeRate;

    @Schema(description = "价格模式")
    private String priceMode;
}

