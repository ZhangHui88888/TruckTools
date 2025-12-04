package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 产品查询参数
 */
@Data
@Schema(description = "产品查询参数")
public class ProductQueryParam {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "关键词(OE号/XK号)")
    private String keyword;

    @Schema(description = "品牌缩写")
    private String brandCode;

    @Schema(description = "OE号(精确搜索)")
    private String oeNo;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方向: asc/desc")
    private String sortOrder;
}

