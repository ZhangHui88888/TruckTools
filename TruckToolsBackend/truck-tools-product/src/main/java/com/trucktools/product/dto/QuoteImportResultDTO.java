package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报价导入结果DTO
 */
@Data
@Schema(description = "报价导入结果")
public class QuoteImportResultDTO {

    @Schema(description = "总行数")
    private Integer totalRows;

    @Schema(description = "匹配成功数")
    private Integer matchedCount;

    @Schema(description = "未匹配数")
    private Integer unmatchedCount;

    @Schema(description = "客户报价总计(USD)")
    private BigDecimal customerTotalUsd;

    @Schema(description = "我方报价总计(USD)")
    private BigDecimal ourTotalUsd;

    @Schema(description = "总差异(USD)")
    private BigDecimal totalDiffUsd;

    @Schema(description = "汇率")
    private BigDecimal exchangeRate;

    @Schema(description = "价格模式")
    private String priceMode;

    @Schema(description = "详细列表")
    private List<QuoteImportItemDTO> items;
}

