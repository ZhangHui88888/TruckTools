package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报价请求DTO
 */
@Data
@Schema(description = "报价请求")
public class QuoteRequestDTO {

    @Schema(description = "报价项列表")
    private List<QuoteItemDTO> items;

    @Schema(description = "价格模式: min/avg/max")
    private String priceMode = "avg";

    @Schema(description = "汇率(RMB/USD)")
    private BigDecimal exchangeRate = new BigDecimal("7.2");

    @Schema(description = "默认利润率(%)")
    private BigDecimal defaultProfitRate;

    @Schema(description = "税率(%)")
    private BigDecimal taxRate = new BigDecimal("10");

    @Schema(description = "FOB费率(%)")
    private BigDecimal fobRate = new BigDecimal("15");

    @Schema(description = "全局含税")
    private Boolean includeTax = false;

    @Schema(description = "全局FOB")
    private Boolean isFob = false;
}

