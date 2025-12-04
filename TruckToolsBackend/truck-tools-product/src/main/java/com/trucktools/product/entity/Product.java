package com.trucktools.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 产品实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
@Schema(description = "产品")
public class Product extends BaseEntity {

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "品牌缩写(MB/VL/SC等)")
    private String brandCode;

    @Schema(description = "品牌全称")
    private String brandName;

    @Schema(description = "内部编号")
    private String xkNo;

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

