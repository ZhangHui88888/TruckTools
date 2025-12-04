package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 品牌VO
 */
@Data
@Schema(description = "品牌信息")
public class BrandVO {

    @Schema(description = "品牌缩写")
    private String brandCode;

    @Schema(description = "品牌全称")
    private String brandName;

    @Schema(description = "产品数量")
    private Integer productCount;
}

