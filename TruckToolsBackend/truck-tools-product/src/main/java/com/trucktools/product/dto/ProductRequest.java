package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 产品创建/更新请求
 */
@Data
@Schema(description = "产品请求")
public class ProductRequest {

    @Schema(description = "品牌缩写")
    private String brandCode;

    @Schema(description = "品牌全称")
    private String brandName;

    @NotBlank(message = "内部编号不能为空")
    @Schema(description = "内部编号")
    private String xkNo;

    @NotBlank(message = "OE编号不能为空")
    @Schema(description = "OE编号")
    private String oeNo;

    @Schema(description = "图片路径")
    private String imagePath;

    @Schema(description = "最低价(RMB)")
    private BigDecimal priceMin;

    @Schema(description = "最高价(RMB)")
    private BigDecimal priceMax;

    @Schema(description = "平均价(RMB)")
    private BigDecimal priceAvg;

    @Schema(description = "备注")
    private String remark;
}

