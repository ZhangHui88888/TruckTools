package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 执行导入请求
 */
@Data
@Schema(description = "执行导入请求")
public class ImportExecuteRequest {

    @Schema(description = "导入模式: append=追加, overwrite=覆盖, merge=合并")
    private String importMode;

    @Schema(description = "重复数据处理方式: skip=跳过, update=更新")
    private String duplicateAction;
}

