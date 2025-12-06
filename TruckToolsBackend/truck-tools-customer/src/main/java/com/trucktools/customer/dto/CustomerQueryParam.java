package com.trucktools.customer.dto;

import com.trucktools.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户查询参数")
public class CustomerQueryParam extends PageQuery {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "关键词搜索")
    private String keyword;

    @Schema(description = "优先级")
    private Integer priority;
    
    @Schema(description = "优先级列表(逗号分隔)")
    private String priorities;

    @Schema(description = "国家")
    private String country;
    
    @Schema(description = "国家列表(逗号分隔)")
    private String countries;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "会面开始时间")
    private String startDate;

    @Schema(description = "会面结束时间")
    private String endDate;
}

