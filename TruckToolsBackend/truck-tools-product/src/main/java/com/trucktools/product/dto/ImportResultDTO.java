package com.trucktools.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 导入结果DTO
 */
@Data
@Schema(description = "导入结果")
public class ImportResultDTO {

    @Schema(description = "导入ID")
    private String importId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "品牌列表(Sheet信息)")
    private List<BrandSheetInfo> brandSheets;

    @Schema(description = "总产品数")
    private Integer totalProducts;

    @Schema(description = "总图片数")
    private Integer totalImages;

    @Schema(description = "预览数据")
    private List<Map<String, Object>> previewData;

    /**
     * 品牌Sheet信息
     */
    @Data
    @Schema(description = "品牌Sheet信息")
    public static class BrandSheetInfo {
        @Schema(description = "Sheet名称")
        private String sheetName;
        
        @Schema(description = "品牌缩写")
        private String brandCode;
        
        @Schema(description = "品牌全称")
        private String brandName;
        
        @Schema(description = "产品数量")
        private Integer productCount;
        
        @Schema(description = "图片数量")
        private Integer imageCount;
    }
}

