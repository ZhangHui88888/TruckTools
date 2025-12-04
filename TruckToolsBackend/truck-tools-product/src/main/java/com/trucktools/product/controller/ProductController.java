package com.trucktools.product.controller;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.product.dto.*;
import com.trucktools.product.service.ProductService;
import com.trucktools.product.service.ExcelImportService;
import com.trucktools.product.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 产品管理控制器
 */
@Tag(name = "产品管理", description = "产品CRUD、导入导出、报价计算")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ExcelImportService excelImportService;
    private final QuoteService quoteService;

    @Operation(summary = "获取产品列表")
    @GetMapping
    public Result<PageResult<ProductVO>> getList(ProductQueryParam param) {
        param.setUserId(SecurityUtils.getCurrentUserId());
        PageResult<ProductVO> result = productService.getPage(param);
        return Result.success(result);
    }

    @Operation(summary = "获取产品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> getDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        ProductVO vo = productService.getDetail(userId, id);
        return Result.success(vo);
    }

    @Operation(summary = "创建产品")
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody ProductRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long id = productService.create(userId, request);
        return Result.success(Map.of("id", id.toString()));
    }

    @Operation(summary = "更新产品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        productService.update(userId, id, request);
        return Result.success();
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        productService.delete(userId, id);
        return Result.success();
    }

    @Operation(summary = "批量删除产品")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody Map<String, List<Long>> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> ids = request.get("ids");
        productService.batchDelete(userId, ids);
        return Result.success();
    }

    @Operation(summary = "获取品牌列表")
    @GetMapping("/brands")
    public Result<List<BrandVO>> getBrands() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<BrandVO> brands = productService.getBrandList(userId);
        return Result.success(brands);
    }

    @Operation(summary = "按OE号搜索产品")
    @GetMapping("/search")
    public Result<List<ProductVO>> searchByOeNo(@RequestParam String oe) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ProductVO> products = productService.searchByOeNo(userId, oe);
        return Result.success(products);
    }

    // ===================== Excel导入 =====================

    @Operation(summary = "下载产品导入模板")
    @GetMapping("/import/template")
    public void downloadImportTemplate(HttpServletResponse response) {
        excelImportService.downloadTemplate(response);
    }

    @Operation(summary = "上传Excel并解析预览")
    @PostMapping("/import/upload")
    public Result<ImportResultDTO> uploadImport(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        ImportResultDTO result = excelImportService.uploadAndParse(userId, file);
        return Result.success(result);
    }

    @Operation(summary = "执行导入")
    @PostMapping("/import/{importId}/execute")
    public Result<ImportStatusDTO> executeImport(@PathVariable String importId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ImportStatusDTO status = excelImportService.executeImport(userId, importId);
        return Result.success(status);
    }

    @Operation(summary = "获取导入状态")
    @GetMapping("/import/{importId}/status")
    public Result<ImportStatusDTO> getImportStatus(@PathVariable String importId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ImportStatusDTO status = excelImportService.getImportStatus(userId, importId);
        return Result.success(status);
    }

    // ===================== 报价计算 =====================

    @Operation(summary = "计算报价")
    @PostMapping("/quote/calculate")
    public Result<QuoteResultDTO> calculateQuote(@RequestBody QuoteRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        QuoteResultDTO result = quoteService.calculate(userId, request);
        return Result.success(result);
    }

    @Operation(summary = "导出报价单")
    @PostMapping("/quote/export")
    public void exportQuote(@RequestBody QuoteRequestDTO request, HttpServletResponse response) {
        Long userId = SecurityUtils.getCurrentUserId();
        quoteService.exportQuote(userId, request, response);
    }

    @Operation(summary = "获取推荐利润率")
    @GetMapping("/quote/profit-rate")
    public Result<Map<String, Object>> getProfitRate(@RequestParam(defaultValue = "1") Integer quantity) {
        java.math.BigDecimal rate = quoteService.getRecommendedProfitRate(quantity);
        return Result.success(Map.of("quantity", quantity, "profitRate", rate));
    }

    // ===================== 报价导入 =====================

    @Operation(summary = "上传报价Excel并解析匹配")
    @PostMapping("/quote/import/parse")
    public Result<QuoteImportResultDTO> parseQuoteImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "priceMode", defaultValue = "avg") String priceMode,
            @RequestParam(value = "exchangeRate", defaultValue = "7.2") java.math.BigDecimal exchangeRate,
            @RequestParam(value = "defaultProfitRate", defaultValue = "10") java.math.BigDecimal defaultProfitRate,
            @RequestParam(value = "includeTax", defaultValue = "false") Boolean includeTax,
            @RequestParam(value = "isFob", defaultValue = "false") Boolean isFob,
            @RequestParam(value = "customerCurrency", defaultValue = "USD") String customerCurrency) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        QuoteImportRequestDTO request = new QuoteImportRequestDTO();
        request.setPriceMode(priceMode);
        request.setExchangeRate(exchangeRate);
        request.setDefaultProfitRate(defaultProfitRate);
        request.setIncludeTax(includeTax);
        request.setIsFob(isFob);
        request.setCustomerCurrency(customerCurrency);
        
        QuoteImportResultDTO result = quoteService.parseQuoteExcel(userId, file, request);
        return Result.success(result);
    }

    @Operation(summary = "重新计算报价导入结果")
    @PostMapping("/quote/import/recalculate")
    public Result<QuoteImportResultDTO> recalculateQuoteImport(@RequestBody QuoteImportRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        QuoteImportResultDTO result = quoteService.recalculateQuoteImport(userId, request);
        return Result.success(result);
    }

    @Operation(summary = "导出报价对比Excel")
    @PostMapping("/quote/import/export")
    public void exportQuoteCompare(@RequestBody QuoteImportRequestDTO request, HttpServletResponse response) {
        Long userId = SecurityUtils.getCurrentUserId();
        quoteService.exportQuoteCompare(userId, request, response);
    }
}

