package com.trucktools.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 */
@Data
@Schema(description = "分页查询参数")
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", defaultValue = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方向", allowableValues = {"asc", "desc"})
    private String sortOrder = "desc";

    /**
     * 获取分页偏移量
     */
    public long getOffset() {
        return (long) (page - 1) * pageSize;
    }

    /**
     * 获取排序方向是否为升序
     */
    public boolean isAsc() {
        return "asc".equalsIgnoreCase(sortOrder);
    }
}

