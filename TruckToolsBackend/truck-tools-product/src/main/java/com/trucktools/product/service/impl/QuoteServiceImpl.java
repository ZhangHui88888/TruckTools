package com.trucktools.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.trucktools.common.exception.BusinessException;


import com.trucktools.product.dto.*;
import com.trucktools.product.entity.Product;
import com.trucktools.product.mapper.ProductMapper;
import com.trucktools.product.service.QuoteService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 报价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final ProductMapper productMapper;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;



    @Value("${app.upload.base-url:http://localhost:8080}")
    private String uploadBaseUrl;

    // 数量阶梯利润率
    private static final BigDecimal[][] PROFIT_TIERS = {
            {BigDecimal.valueOf(1), BigDecimal.valueOf(99), BigDecimal.valueOf(10)},
            {BigDecimal.valueOf(100), BigDecimal.valueOf(199), BigDecimal.valueOf(6)},
            {BigDecimal.valueOf(200), BigDecimal.valueOf(499), BigDecimal.valueOf(3)},
            {BigDecimal.valueOf(500), BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.ZERO}
    };

    @Override
    public QuoteResultDTO calculate(Long userId, QuoteRequestDTO request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("报价项不能为空");
        }

        BigDecimal exchangeRate = request.getExchangeRate();
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            exchangeRate = new BigDecimal("7.2");
        }

        BigDecimal taxRate = request.getTaxRate();
        if (taxRate == null) {
            taxRate = new BigDecimal("10");
        }

        BigDecimal fobRate = request.getFobRate();
        if (fobRate == null) {
            fobRate = new BigDecimal("15");
        }

        String priceMode = request.getPriceMode();
        if (StrUtil.isBlank(priceMode)) {
            priceMode = "avg";
        }

        List<QuoteItemDTO> resultItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (QuoteItemDTO item : request.getItems()) {
            QuoteItemDTO resultItem = calculateItem(item, priceMode, exchangeRate, taxRate, fobRate,
                    request.getIncludeTax(), request.getIsFob(), request.getDefaultProfitRate());
            resultItems.add(resultItem);
            if (resultItem.getSubtotal() != null) {
                totalAmount = totalAmount.add(resultItem.getSubtotal());
            }
        }

        QuoteResultDTO result = new QuoteResultDTO();
        result.setItems(resultItems);
        result.setTotalCount(resultItems.size());
        result.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        result.setExchangeRate(exchangeRate);
        result.setPriceMode(priceMode);

        return result;
    }

    private QuoteItemDTO calculateItem(QuoteItemDTO item, String priceMode, BigDecimal exchangeRate,
                                        BigDecimal taxRate, BigDecimal fobRate,
                                        Boolean globalIncludeTax, Boolean globalIsFob,
                                        BigDecimal defaultProfitRate) {
        QuoteItemDTO result = new QuoteItemDTO();
        result.setProductId(item.getProductId());
        result.setXkNo(item.getXkNo());
        result.setOeNo(item.getOeNo());
        result.setImageUrl(item.getImageUrl());
        result.setBrandCode(item.getBrandCode());
        result.setRemark(item.getRemark());

        Integer quantity = item.getQuantity();
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }
        result.setQuantity(quantity);

        // 获取基础价格(RMB)
        BigDecimal priceRmb = item.getPriceRmb();
        result.setPriceRmb(priceRmb);

        if (priceRmb == null || priceRmb.compareTo(BigDecimal.ZERO) <= 0) {
            result.setPriceUsd(BigDecimal.ZERO);
            result.setRecommendedPrice(BigDecimal.ZERO);
            result.setFinalPrice(BigDecimal.ZERO);
            result.setSubtotal(BigDecimal.ZERO);
            return result;
        }

        // 汇率换算
        BigDecimal priceUsd = priceRmb.divide(exchangeRate, 4, RoundingMode.HALF_UP);
        result.setPriceUsd(priceUsd.setScale(2, RoundingMode.HALF_UP));

        // 获取利润率
        BigDecimal profitRate = item.getProfitRate();
        if (profitRate == null) {
            profitRate = defaultProfitRate != null ? defaultProfitRate : getRecommendedProfitRate(quantity);
        }
        result.setProfitRate(profitRate);

        // 利润加成
        BigDecimal profitMultiplier = BigDecimal.ONE.add(profitRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal afterProfit = priceUsd.multiply(profitMultiplier);

        // 含税/FOB处理
        Boolean includeTax = item.getIncludeTax() != null ? item.getIncludeTax() : globalIncludeTax;
        Boolean isFob = item.getIsFob() != null ? item.getIsFob() : globalIsFob;

        result.setIncludeTax(includeTax);
        result.setIsFob(isFob);

        BigDecimal recommendedPrice;
        if (Boolean.TRUE.equals(isFob)) {
            // FOB价格(不含税)
            BigDecimal fobMultiplier = BigDecimal.ONE.add(fobRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            recommendedPrice = afterProfit.multiply(fobMultiplier);
        } else if (Boolean.TRUE.equals(includeTax)) {
            // 含税价格
            BigDecimal taxMultiplier = BigDecimal.ONE.add(taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            recommendedPrice = afterProfit.multiply(taxMultiplier);
        } else {
            recommendedPrice = afterProfit;
        }

        recommendedPrice = recommendedPrice.setScale(2, RoundingMode.HALF_UP);
        result.setRecommendedPrice(recommendedPrice);

        // 最终价格(如果用户没有手动设置，使用推荐价格)
        BigDecimal finalPrice = item.getFinalPrice() != null ? item.getFinalPrice() : recommendedPrice;
        result.setFinalPrice(finalPrice);

        // 计算小计
        BigDecimal subtotal = finalPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        result.setSubtotal(subtotal);

        return result;
    }

    @Override
    public void exportQuote(Long userId, QuoteRequestDTO request, HttpServletResponse response) {
        // 先计算报价
        QuoteResultDTO quoteResult = calculate(userId, request);

        try {
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("报价单");

            // 设置列宽
            sheet.setColumnWidth(0, 2000);  // NO.
            sheet.setColumnWidth(1, 4000);  // XK NO.
            sheet.setColumnWidth(2, 4000);  // OE NO.
            sheet.setColumnWidth(3, 3667);  // PICTURE
            sheet.setColumnWidth(4, 3500);  // UNIT PRICE

            // 创建样式
            CellStyle titleStyle = createSimpleTitleStyle(workbook);
            CellStyle headerStyle = createSimpleHeaderStyle(workbook);
            CellStyle dataStyle = createSimpleDataStyle(workbook);
            CellStyle priceStyle = createSimplePriceStyle(workbook, dataStyle);
            CellStyle noPriceStyle = createSimpleNoPriceStyle(workbook, dataStyle);

            int rowNum = 0;

            // 标题行 (行高缩小一倍)
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(15);
            Cell titleCell = titleRow.createCell(0);
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.M.d"));
            titleCell.setCellValue("PRICE QUOTATION -- " + dateStr);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // 空行
            rowNum++;

            // 表头行 (行高缩小一倍)
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(12.5f);
            String[] headers = {"NO.", "XK NO.", "OE NO.", "PICTURE", "UNIT PRICE"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            int no = 1;
            for (QuoteItemDTO item : quoteResult.getItems()) {
                Row dataRow = sheet.createRow(rowNum);
                dataRow.setHeightInPoints(60); // 统一行高 (缩小一倍)

                // NO.
                Cell noCell = dataRow.createCell(0);
                noCell.setCellValue(no++);
                noCell.setCellStyle(dataStyle);

                // XK NO.
                Cell xkCell = dataRow.createCell(1);
                xkCell.setCellValue(item.getXkNo() != null ? item.getXkNo() : "");
                xkCell.setCellStyle(dataStyle);

                // OE NO. - 将斜杠替换为换行符，去除多余空格避免空行
                Cell oeCell = dataRow.createCell(2);
                String oeNo = item.getOeNo() != null ? item.getOeNo() : "";
                // 先清理空格，再替换斜杠为换行符，避免产生空行
                oeNo = oeNo.trim()
                           .replaceAll("\\s*/\\s*", "/")  // 去除斜杠前后的空格
                           .replace("/", "\n");            // 将 / 替换为换行符
                oeCell.setCellValue(oeNo);
                oeCell.setCellStyle(dataStyle);

                // PICTURE
                Cell picCell = dataRow.createCell(3);
                picCell.setCellStyle(dataStyle);
                insertUniformPicture(workbook, drawing, item.getImageUrl(), 3, rowNum, item.getOeNo());

                // UNIT PRICE
                Cell priceCell = dataRow.createCell(4);
                if (item.getFinalPrice() != null && item.getFinalPrice().compareTo(BigDecimal.ZERO) > 0) {
                    // 有有效价格，正常显示
                    priceCell.setCellValue("$" + item.getFinalPrice().setScale(2, RoundingMode.HALF_UP));
                    priceCell.setCellStyle(priceStyle);
                } else {
                    // 无价格或价格为0，特殊标注
                    priceCell.setCellValue("TBD");
                    priceCell.setCellStyle(noPriceStyle);
                }

                rowNum++;
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("报价单_" + dateStr + ".xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 输出文件
            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (IOException e) {
            log.error("导出报价单失败", e);
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getRecommendedProfitRate(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }

        BigDecimal qty = BigDecimal.valueOf(quantity);
        for (BigDecimal[] tier : PROFIT_TIERS) {
            if (qty.compareTo(tier[0]) >= 0 && qty.compareTo(tier[1]) <= 0) {
                return tier[2];
            }
        }

        return BigDecimal.valueOf(10); // 默认10%
    }



    @Override
    public QuoteImportResultDTO parseQuoteExcel(Long userId, MultipartFile file, QuoteImportRequestDTO request) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传文件");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException("请上传Excel文件(.xlsx或.xls)");
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Excel文件没有工作表");
            }

            // 查找表头行，定位OE NO.和UNIT PRICE列
            int oeNoColIndex = -1;
            int unitPriceColIndex = -1;
            int headerRowIndex = -1;

            // 遍历前10行查找表头
            for (int i = 0; i <= Math.min(10, sheet.getLastRowNum()); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) continue;

                    String cellValue = getCellStringValue(cell).trim().toUpperCase();
                    
                    // 匹配OE NO.列
                    if (cellValue.contains("OE") && (cellValue.contains("NO") || cellValue.contains("号"))) {
                        oeNoColIndex = j;
                        headerRowIndex = i;
                    }
                    // 匹配UNIT PRICE列
                    if (cellValue.contains("PRICE") || cellValue.contains("价格") || cellValue.contains("单价")) {
                        unitPriceColIndex = j;
                        headerRowIndex = i;
                    }
                }
                
                // 如果找到了两个必要的列，停止搜索
                if (oeNoColIndex >= 0 && unitPriceColIndex >= 0) {
                    break;
                }
            }

            if (oeNoColIndex < 0) {
                throw new BusinessException("未找到OE NO.列，请确保Excel中包含OE NO.或OE号列");
            }
            if (unitPriceColIndex < 0) {
                throw new BusinessException("未找到UNIT PRICE列，请确保Excel中包含PRICE或单价列");
            }

            // 解析参数
            BigDecimal exchangeRate = request.getExchangeRate();
            if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
                exchangeRate = new BigDecimal("7.2");
            }
            
            String priceMode = request.getPriceMode();
            if (StrUtil.isBlank(priceMode)) {
                priceMode = "avg";
            }
            
            String customerCurrency = request.getCustomerCurrency();
            if (StrUtil.isBlank(customerCurrency)) {
                customerCurrency = "USD";
            }

            List<QuoteImportItemDTO> items = new ArrayList<>();
            int matchedCount = 0;
            BigDecimal customerTotalUsd = BigDecimal.ZERO;
            BigDecimal ourTotalUsd = BigDecimal.ZERO;

            // 从表头下一行开始读取数据
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell oeNoCell = row.getCell(oeNoColIndex);
                Cell priceCell = row.getCell(unitPriceColIndex);

                String oeNo = getCellStringValue(oeNoCell).trim();
                if (StrUtil.isBlank(oeNo)) continue;
                
                // 清除 OE NO. 中的连字符（用于显示和匹配）
                String cleanedOeNo = oeNo.replace("-", "").trim();

                QuoteImportItemDTO item = new QuoteImportItemDTO();
                item.setRowIndex(i + 1); // Excel行号从1开始
                item.setOeNo(cleanedOeNo); // 使用清除-后的值

                // 解析客户报价
                String priceRaw = getCellStringValue(priceCell).trim();
                item.setCustomerPriceRaw(priceRaw);
                
                BigDecimal customerPrice = parsePriceValue(priceRaw);
                if (customerPrice != null && customerPrice.compareTo(BigDecimal.ZERO) > 0) {
                    // 如果客户报价是RMB，转换为USD
                    if ("RMB".equalsIgnoreCase(customerCurrency)) {
                        item.setCustomerPriceUsd(customerPrice.divide(exchangeRate, 4, RoundingMode.HALF_UP));
                    } else {
                        item.setCustomerPriceUsd(customerPrice);
                    }
                    customerTotalUsd = customerTotalUsd.add(item.getCustomerPriceUsd());
                }

                // 在数据库中查找匹配的产品（使用清理后的OE NO.进行模糊匹配）
                Product product = productMapper.selectByOeNoFuzzy(userId, cleanedOeNo);
                if (product != null) {
                    item.setMatched(true);
                    item.setProductId(product.getId().toString());
                    item.setXkNo(product.getXkNo());
                    item.setBrandCode(product.getBrandCode());
                    
                    // 设置图片URL
                    if (StrUtil.isNotBlank(product.getImagePath())) {
                        item.setImageUrl(uploadBaseUrl + product.getImagePath());
                    }

                    // 设置三种价格
                    item.setOurPriceMin(product.getPriceMin());
                    item.setOurPriceAvg(product.getPriceAvg());
                    item.setOurPriceMax(product.getPriceMax());

                    // 根据价格模式选择价格
                    BigDecimal selectedPrice = selectPrice(product, priceMode);
                    item.setOurPriceRmb(selectedPrice);

                    // 计算我方价格
                    if (selectedPrice != null && selectedPrice.compareTo(BigDecimal.ZERO) > 0) {
                        // 计算最终报价
                        QuoteImportItemDTO calculatedItem = calculateImportItem(item, request, exchangeRate);
                        items.add(calculatedItem);
                        
                        if (calculatedItem.getCalculatedPrice() != null) {
                            ourTotalUsd = ourTotalUsd.add(calculatedItem.getCalculatedPrice());
                        }
                    } else {
                        items.add(item);
                    }
                    matchedCount++;
                } else {
                    item.setMatched(false);
                    item.setRemark("未找到匹配产品");
                    items.add(item);
                }
            }

            // 构建返回结果
            QuoteImportResultDTO result = new QuoteImportResultDTO();
            result.setTotalRows(items.size());
            result.setMatchedCount(matchedCount);
            result.setUnmatchedCount(items.size() - matchedCount);
            result.setCustomerTotalUsd(customerTotalUsd.setScale(2, RoundingMode.HALF_UP));
            result.setOurTotalUsd(ourTotalUsd.setScale(2, RoundingMode.HALF_UP));
            result.setTotalDiffUsd(ourTotalUsd.subtract(customerTotalUsd).setScale(2, RoundingMode.HALF_UP));
            result.setExchangeRate(exchangeRate);
            result.setPriceMode(priceMode);
            result.setItems(items);

            return result;

        } catch (IOException e) {
            log.error("解析Excel文件失败", e);
            throw new BusinessException("解析Excel文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据价格模式选择价格
     */
    private BigDecimal selectPrice(Product product, String priceMode) {
        switch (priceMode) {
            case "min":
                return product.getPriceMin();
            case "max":
                return product.getPriceMax();
            default:
                return product.getPriceAvg();
        }
    }

    /**
     * 计算导入项的报价
     */
    private QuoteImportItemDTO calculateImportItem(QuoteImportItemDTO item, QuoteImportRequestDTO request, BigDecimal exchangeRate) {
        BigDecimal priceRmb = item.getOurPriceRmb();
        if (priceRmb == null || priceRmb.compareTo(BigDecimal.ZERO) <= 0) {
            return item;
        }

        // 汇率换算
        BigDecimal priceUsd = priceRmb.divide(exchangeRate, 4, RoundingMode.HALF_UP);
        item.setOurPriceUsd(priceUsd.setScale(2, RoundingMode.HALF_UP));

        // 获取利润率
        BigDecimal profitRate = item.getProfitRate();
        if (profitRate == null) {
            profitRate = request.getDefaultProfitRate();
            if (profitRate == null) {
                Integer quantity = item.getQuantity() != null ? item.getQuantity() : 1;
                profitRate = getRecommendedProfitRate(quantity);
            }
        }
        item.setProfitRate(profitRate);

        // 利润加成
        BigDecimal profitMultiplier = BigDecimal.ONE.add(profitRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal afterProfit = priceUsd.multiply(profitMultiplier);

        // 含税/FOB处理
        Boolean includeTax = item.getIncludeTax() != null ? item.getIncludeTax() : request.getIncludeTax();
        Boolean isFob = item.getIsFob() != null ? item.getIsFob() : request.getIsFob();
        item.setIncludeTax(includeTax);
        item.setIsFob(isFob);

        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : new BigDecimal("10");
        BigDecimal fobRate = request.getFobRate() != null ? request.getFobRate() : new BigDecimal("15");

        BigDecimal calculatedPrice;
        if (Boolean.TRUE.equals(isFob)) {
            BigDecimal fobMultiplier = BigDecimal.ONE.add(fobRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            calculatedPrice = afterProfit.multiply(fobMultiplier);
        } else if (Boolean.TRUE.equals(includeTax)) {
            BigDecimal taxMultiplier = BigDecimal.ONE.add(taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            calculatedPrice = afterProfit.multiply(taxMultiplier);
        } else {
            calculatedPrice = afterProfit;
        }

        calculatedPrice = calculatedPrice.setScale(2, RoundingMode.HALF_UP);
        item.setCalculatedPrice(calculatedPrice);

        // 计算与客户报价的差异
        if (item.getCustomerPriceUsd() != null && item.getCustomerPriceUsd().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = calculatedPrice.subtract(item.getCustomerPriceUsd());
            item.setPriceDiff(diff.setScale(2, RoundingMode.HALF_UP));
            
            BigDecimal diffPercent = diff.divide(item.getCustomerPriceUsd(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            item.setPriceDiffPercent(diffPercent.setScale(2, RoundingMode.HALF_UP));
        }

        return item;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // 避免科学计数法
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    /**
     * 解析价格值（支持带$或¥符号的价格）
     */
    private BigDecimal parsePriceValue(String priceStr) {
        if (StrUtil.isBlank(priceStr)) {
            return null;
        }
        
        // 移除货币符号和空格
        String cleanStr = priceStr.replaceAll("[\\$¥￥,\\s]", "").trim();
        
        // 使用正则提取数字
        Pattern pattern = Pattern.compile("([0-9]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(cleanStr);
        
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public QuoteImportResultDTO recalculateQuoteImport(Long userId, QuoteImportRequestDTO request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("报价项不能为空");
        }

        BigDecimal exchangeRate = request.getExchangeRate();
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            exchangeRate = new BigDecimal("7.2");
        }

        String priceMode = request.getPriceMode();
        if (StrUtil.isBlank(priceMode)) {
            priceMode = "avg";
        }

        List<QuoteImportItemDTO> resultItems = new ArrayList<>();
        int matchedCount = 0;
        BigDecimal customerTotalUsd = BigDecimal.ZERO;
        BigDecimal ourTotalUsd = BigDecimal.ZERO;

        for (QuoteImportItemDTO item : request.getItems()) {
            // 根据价格模式重新选择价格
            BigDecimal selectedPrice;
            switch (priceMode) {
                case "min":
                    selectedPrice = item.getOurPriceMin();
                    break;
                case "max":
                    selectedPrice = item.getOurPriceMax();
                    break;
                default:
                    selectedPrice = item.getOurPriceAvg();
            }
            item.setOurPriceRmb(selectedPrice);

            // 重新计算
            if (Boolean.TRUE.equals(item.getMatched()) && selectedPrice != null && selectedPrice.compareTo(BigDecimal.ZERO) > 0) {
                QuoteImportItemDTO calculatedItem = calculateImportItem(item, request, exchangeRate);
                resultItems.add(calculatedItem);
                
                if (calculatedItem.getCalculatedPrice() != null) {
                    ourTotalUsd = ourTotalUsd.add(calculatedItem.getCalculatedPrice());
                }
                matchedCount++;
            } else {
                resultItems.add(item);
                if (Boolean.TRUE.equals(item.getMatched())) {
                    matchedCount++;
                }
            }

            if (item.getCustomerPriceUsd() != null) {
                customerTotalUsd = customerTotalUsd.add(item.getCustomerPriceUsd());
            }
        }

        QuoteImportResultDTO result = new QuoteImportResultDTO();
        result.setTotalRows(resultItems.size());
        result.setMatchedCount(matchedCount);
        result.setUnmatchedCount(resultItems.size() - matchedCount);
        result.setCustomerTotalUsd(customerTotalUsd.setScale(2, RoundingMode.HALF_UP));
        result.setOurTotalUsd(ourTotalUsd.setScale(2, RoundingMode.HALF_UP));
        result.setTotalDiffUsd(ourTotalUsd.subtract(customerTotalUsd).setScale(2, RoundingMode.HALF_UP));
        result.setExchangeRate(exchangeRate);
        result.setPriceMode(priceMode);
        result.setItems(resultItems);

        return result;
    }

    @Override
    public void exportQuoteCompare(Long userId, QuoteImportRequestDTO request, HttpServletResponse response) {
        // 先重新计算
        QuoteImportResultDTO result = recalculateQuoteImport(userId, request);

        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("报价对比");

            // 设置列宽
            sheet.setColumnWidth(0, 2000);  // NO.
            sheet.setColumnWidth(1, 4000);  // XK NO.
            sheet.setColumnWidth(2, 5000);  // OE NO.
            sheet.setColumnWidth(3, 11000); // 图片
            sheet.setColumnWidth(4, 3500);  // 客户报价
            sheet.setColumnWidth(5, 3500);  // 我方成本价(RMB)
            sheet.setColumnWidth(6, 3500);  // 我方报价(USD)
            sheet.setColumnWidth(7, 3000);  // 差异
            sheet.setColumnWidth(8, 2500);  // 差异%
            sheet.setColumnWidth(9, 3000);  // 备注

            // 创建样式
            CellStyle titleStyle = createSimpleTitleStyle(workbook);
            CellStyle headerStyle = createSimpleHeaderStyle(workbook);
            CellStyle dataStyle = createSimpleDataStyle(workbook);
            CellStyle priceStyle = createSimplePriceStyle(workbook, dataStyle);
            
            // 创建差异样式
            CellStyle positiveStyle = workbook.createCellStyle();
            positiveStyle.cloneStyleFrom(priceStyle);
            Font positiveFont = workbook.createFont();
            positiveFont.setColor(IndexedColors.GREEN.getIndex());
            positiveFont.setFontHeightInPoints((short) 10);
            positiveFont.setFontName("Arial");
            positiveStyle.setFont(positiveFont);

            CellStyle negativeStyle = workbook.createCellStyle();
            negativeStyle.cloneStyleFrom(priceStyle);
            Font negativeFont = workbook.createFont();
            negativeFont.setColor(IndexedColors.RED.getIndex());
            negativeFont.setFontHeightInPoints((short) 10);
            negativeFont.setFontName("Arial");
            negativeStyle.setFont(negativeFont);

            CellStyle unmatchedStyle = workbook.createCellStyle();
            unmatchedStyle.cloneStyleFrom(dataStyle);
            unmatchedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            unmatchedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowNum = 0;

            // 标题行
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.M.d"));
            titleCell.setCellValue("PRICE QUOTATION COMPARISON -- " + dateStr);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // 空行
            rowNum++;

            // 汇总信息行
            Row summaryRow = sheet.createRow(rowNum++);
            summaryRow.setHeightInPoints(20);
            Cell summaryCell = summaryRow.createCell(0);
            String summary = String.format("Exchange Rate: %.2f | Price Mode: %s | Total: %d | Matched: %d | Unmatched: %d",
                    result.getExchangeRate(), 
                    "min".equals(result.getPriceMode()) ? "MIN" : ("max".equals(result.getPriceMode()) ? "MAX" : "AVG"),
                    result.getTotalRows(), result.getMatchedCount(), result.getUnmatchedCount());
            summaryCell.setCellValue(summary);
            summaryCell.setCellStyle(dataStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 9));

            // 空行
            rowNum++;

            // 表头行
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(25);
            String[] headers = {"NO.", "XK NO.", "OE NO.", "PICTURE", "Customer($)", "Cost(¥)", "Our Price($)", "Diff($)", "Diff%", "Remark"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            int no = 1;
            for (QuoteImportItemDTO item : result.getItems()) {
                Row dataRow = sheet.createRow(rowNum);
                dataRow.setHeightInPoints(120); // 统一行高

                boolean isMatched = Boolean.TRUE.equals(item.getMatched());
                CellStyle rowStyle = isMatched ? dataStyle : unmatchedStyle;
                CellStyle rowPriceStyle = isMatched ? priceStyle : unmatchedStyle;

                // NO.
                Cell noCell = dataRow.createCell(0);
                noCell.setCellValue(no++);
                noCell.setCellStyle(rowStyle);

                // XK NO.
                Cell xkCell = dataRow.createCell(1);
                xkCell.setCellValue(item.getXkNo() != null ? item.getXkNo() : "-");
                xkCell.setCellStyle(rowStyle);

                // OE NO. - 去除多余空格避免空行
                Cell oeCell = dataRow.createCell(2);
                String oeNo = item.getOeNo() != null ? item.getOeNo() : "";
                oeNo = oeNo.trim()
                           .replaceAll("\\s*/\\s*", "/")
                           .replace("/", "\n");
                oeCell.setCellValue(oeNo);
                oeCell.setCellStyle(rowStyle);

                // 图片
                Cell picCell = dataRow.createCell(3);
                picCell.setCellStyle(rowStyle);
                if (isMatched) {
                    insertUniformPicture(workbook, drawing, item.getImageUrl(), 3, rowNum, item.getOeNo());
                }

                // 客户报价
                Cell customerPriceCell = dataRow.createCell(4);
                if (item.getCustomerPriceUsd() != null) {
                    customerPriceCell.setCellValue("$" + item.getCustomerPriceUsd().setScale(2, RoundingMode.HALF_UP));
                } else {
                    customerPriceCell.setCellValue(item.getCustomerPriceRaw() != null ? item.getCustomerPriceRaw() : "-");
                }
                customerPriceCell.setCellStyle(rowPriceStyle);

                // 我方成本价(RMB)
                Cell ourPriceRmbCell = dataRow.createCell(5);
                if (item.getOurPriceRmb() != null) {
                    ourPriceRmbCell.setCellValue("¥" + item.getOurPriceRmb().setScale(2, RoundingMode.HALF_UP));
                } else {
                    ourPriceRmbCell.setCellValue("-");
                }
                ourPriceRmbCell.setCellStyle(rowPriceStyle);

                // 我方报价(USD)
                Cell calculatedPriceCell = dataRow.createCell(6);
                if (item.getCalculatedPrice() != null) {
                    calculatedPriceCell.setCellValue("$" + item.getCalculatedPrice().setScale(2, RoundingMode.HALF_UP));
                } else {
                    calculatedPriceCell.setCellValue("-");
                }
                calculatedPriceCell.setCellStyle(rowPriceStyle);

                // 差异
                Cell diffCell = dataRow.createCell(7);
                if (item.getPriceDiff() != null) {
                    diffCell.setCellValue("$" + item.getPriceDiff().setScale(2, RoundingMode.HALF_UP));
                    diffCell.setCellStyle(item.getPriceDiff().compareTo(BigDecimal.ZERO) >= 0 ? positiveStyle : negativeStyle);
                } else {
                    diffCell.setCellValue("-");
                    diffCell.setCellStyle(rowPriceStyle);
                }

                // 差异%
                Cell diffPercentCell = dataRow.createCell(8);
                if (item.getPriceDiffPercent() != null) {
                    diffPercentCell.setCellValue(item.getPriceDiffPercent().setScale(1, RoundingMode.HALF_UP) + "%");
                    diffPercentCell.setCellStyle(item.getPriceDiffPercent().compareTo(BigDecimal.ZERO) >= 0 ? positiveStyle : negativeStyle);
                } else {
                    diffPercentCell.setCellValue("-");
                    diffPercentCell.setCellStyle(rowPriceStyle);
                }

                // 备注
                Cell remarkCell = dataRow.createCell(9);
                remarkCell.setCellValue(item.getRemark() != null ? item.getRemark() : (isMatched ? "" : "未匹配"));
                remarkCell.setCellStyle(rowStyle);

                rowNum++;
            }

            // 空行
            rowNum++;

            // 汇总行
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(3);
            totalLabelCell.setCellValue("合计:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell customerTotalCell = totalRow.createCell(4);
            customerTotalCell.setCellValue("$" + result.getCustomerTotalUsd());
            customerTotalCell.setCellStyle(headerStyle);

            Cell ourTotalCell = totalRow.createCell(6);
            ourTotalCell.setCellValue("$" + result.getOurTotalUsd());
            ourTotalCell.setCellStyle(headerStyle);

            Cell diffTotalCell = totalRow.createCell(7);
            diffTotalCell.setCellValue("$" + result.getTotalDiffUsd());
            diffTotalCell.setCellStyle(result.getTotalDiffUsd().compareTo(BigDecimal.ZERO) >= 0 ? positiveStyle : negativeStyle);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("报价对比_" + dateStr + ".xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (IOException e) {
            log.error("导出报价对比失败", e);
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 根据图片URL获取本地文件路径
     */
    private String getLocalImagePath(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return null;
        }
        
        // 如果是完整的HTTP URL（如 http://localhost:8080/uploads/products/xxx.jpg）
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // 提取URL中的路径部分
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                String path = url.getPath();
                // 路径如 /uploads/products/2025/12/03/xxx.jpg
                // 需要去掉开头的 /uploads 避免重复
                if (path.startsWith("/uploads/")) {
                    path = path.substring("/uploads".length());
                }
                return uploadPath + path;
            } catch (Exception e) {
                log.warn("解析图片URL失败: {}", imageUrl);
                return null;
            }
        }
        
        // 如果是相对路径（如 /uploads/products/xxx.jpg）
        if (imageUrl.startsWith("/uploads/")) {
            return uploadPath + imageUrl.substring("/uploads".length());
        }
        
        // 如果是相对路径（如 /products/xxx.jpg）
        if (imageUrl.startsWith("/")) {
            return uploadPath + imageUrl;
        }
        
        return null;
    }

    /**
     * 根据文件扩展名获取POI图片类型
     */
    private int getPictureType(String fileName) {
        if (fileName == null) {
            return Workbook.PICTURE_TYPE_JPEG;
        }
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return Workbook.PICTURE_TYPE_PNG;
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return Workbook.PICTURE_TYPE_JPEG;
        } else if (lowerName.endsWith(".bmp") || lowerName.endsWith(".dib")) {
            return Workbook.PICTURE_TYPE_DIB;
        } else if (lowerName.endsWith(".emf")) {
            return Workbook.PICTURE_TYPE_EMF;
        } else if (lowerName.endsWith(".wmf")) {
            return Workbook.PICTURE_TYPE_WMF;
        }
        // GIF、TIFF等格式需要转换为PNG处理
        return Workbook.PICTURE_TYPE_PNG;
    }

    /**
     * 读取图片并转换为Excel支持的格式
     */
    private byte[] readImageForExcel(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                return null;
            }
            
            String lowerPath = imagePath.toLowerCase();
            // 如果是TIFF或GIF格式，需要转换为PNG
            if (lowerPath.endsWith(".tiff") || lowerPath.endsWith(".tif") || lowerPath.endsWith(".gif")) {
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(file);
                if (image != null) {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(image, "PNG", baos);
                    return baos.toByteArray();
                }
            }
            
            // 其他格式直接读取
            return Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            log.warn("读取图片失败: {} - {}", imagePath, e.getMessage());
            return null;
        }
    }

    /**
     * 创建简约标题样式
     */
    private CellStyle createSimpleTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setFontName("Arial");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建简约表头样式 - 青色背景
     */
    private CellStyle createSimpleHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 创建简约数据样式
     */
    private CellStyle createSimpleDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    /**
     * 创建简约价格样式
     */
    private CellStyle createSimplePriceStyle(Workbook workbook, CellStyle baseStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        style.setFont(font);
        return style;
    }

    /**
     * 创建简约无价格样式 - 红色加粗提醒
     */
    private CellStyle createSimpleNoPriceStyle(Workbook workbook, CellStyle baseStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        style.setFont(font);
        return style;
    }

    /**
     * 插入统一大小的图片
     */
    private void insertUniformPicture(Workbook workbook, Drawing<?> drawing, String imageUrl, 
                                      int col, int row, String oeNo) {
        if (StrUtil.isBlank(imageUrl)) {
            return;
        }

        try {
            String imagePath = getLocalImagePath(imageUrl);
            if (imagePath == null) {
                log.warn("无法获取本地图片路径: OE NO={}, imageUrl={}", oeNo, imageUrl);
                return;
            }

            byte[] imageBytes = readImageForExcel(imagePath);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("图片数据为空: OE NO={}, path={}", oeNo, imagePath);
                return;
            }

            int pictureType = getPictureType(imagePath);
            int pictureIdx = workbook.addPicture(imageBytes, pictureType);
            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();

            // 统一图片大小 - 设置固定的起始和结束位置
            anchor.setCol1(col);
            anchor.setRow1(row);
            anchor.setCol2(col + 1);
            anchor.setRow2(row + 1);

            // 设置内边距
            anchor.setDx1(15 * 9525);
            anchor.setDy1(15 * 9525);
            anchor.setDx2(-15 * 9525);
            anchor.setDy2(-15 * 9525);

            // MOVE_AND_RESIZE：图片随单元格调整
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            drawing.createPicture(anchor, pictureIdx);

            log.info("✅ 图片插入成功: OE NO={}", oeNo);
        } catch (Exception e) {
            log.error("插入图片失败: OE NO={}, imageUrl={}", oeNo, imageUrl, e);
        }
    }
}



