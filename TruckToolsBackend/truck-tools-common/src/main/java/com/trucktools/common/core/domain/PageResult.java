package com.trucktools.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "分页信息")
    private Pagination pagination;

    @Data
    @Schema(description = "分页信息")
    public static class Pagination implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "当前页码")
        private Integer page;

        @Schema(description = "每页数量")
        private Integer pageSize;

        @Schema(description = "总记录数")
        private Long total;

        @Schema(description = "总页数")
        private Integer totalPages;
    }

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);

        Pagination pagination = new Pagination();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        pagination.setTotalPages((int) Math.ceil((double) total / pageSize));
        result.setPagination(pagination);

        return result;
    }
}

