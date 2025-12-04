package com.trucktools.product.service;

import com.trucktools.product.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 报价服务接口
 */
public interface QuoteService {

    /**
     * 计算报价
     * @param userId 用户ID
     * @param request 报价请求
     * @return 报价结果
     */
    QuoteResultDTO calculate(Long userId, QuoteRequestDTO request);

    /**
     * 导出报价单Excel
     * @param userId 用户ID
     * @param request 报价请求
     * @param response HTTP响应
     */
    void exportQuote(Long userId, QuoteRequestDTO request, HttpServletResponse response);

    /**
     * 根据数量获取推荐利润率
     * @param quantity 数量
     * @return 利润率(百分比)
     */
    java.math.BigDecimal getRecommendedProfitRate(Integer quantity);

    /**
     * 解析报价Excel并匹配产品
     * @param userId 用户ID
     * @param file Excel文件
     * @param request 报价参数
     * @return 解析结果
     */
    QuoteImportResultDTO parseQuoteExcel(Long userId, MultipartFile file, QuoteImportRequestDTO request);

    /**
     * 重新计算报价导入结果
     * @param userId 用户ID
     * @param request 报价导入请求(包含调整后的参数)
     * @return 计算结果
     */
    QuoteImportResultDTO recalculateQuoteImport(Long userId, QuoteImportRequestDTO request);

    /**
     * 导出报价对比Excel
     * @param userId 用户ID
     * @param request 报价导入请求
     * @param response HTTP响应
     */
    void exportQuoteCompare(Long userId, QuoteImportRequestDTO request, HttpServletResponse response);
}

