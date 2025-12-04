package com.trucktools.product.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.product.dto.ImportResultDTO;
import com.trucktools.product.dto.ImportStatusDTO;
import com.trucktools.product.entity.Product;
import com.trucktools.product.entity.ProductImport;
import com.trucktools.product.mapper.ProductImportMapper;
import com.trucktools.product.mapper.ProductMapper;
import com.trucktools.product.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel导入服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {

    private final ProductMapper productMapper;
    private final ProductImportMapper productImportMapper;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    // 价格解析正则
    private static final Pattern PRICE_RANGE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)[\\-~—](\\d+(?:\\.\\d+)?)");
    private static final Pattern PRICE_SINGLE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    @Override
    public ImportResultDTO uploadAndParse(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            throw new BusinessException("仅支持.xlsx格式的Excel文件");
        }

        try {
            // 保存上传文件
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String savedFileName = IdUtil.fastSimpleUUID() + ".xlsx";
            Path uploadDir = Paths.get(uploadPath, "imports", dateDir);
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(savedFileName);
            file.transferTo(filePath.toFile());

            // 创建导入记录
            ProductImport importRecord = new ProductImport();
            importRecord.setUserId(userId);
            importRecord.setFileName(originalFilename);
            importRecord.setFilePath(filePath.toString());
            importRecord.setFileSize(file.getSize());
            importRecord.setStatus(0);
            productImportMapper.insert(importRecord);

            // 解析Excel
            ImportResultDTO result = parseExcel(filePath.toFile(), userId);
            result.setImportId(String.valueOf(importRecord.getId()));
            result.setFileName(originalFilename);

            // 更新导入记录
            importRecord.setTotalRows(result.getTotalProducts());
            productImportMapper.updateById(importRecord);

            return result;

        } catch (IOException e) {
            log.error("Excel文件处理失败", e);
            throw new BusinessException("文件处理失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportStatusDTO executeImport(Long userId, String importId) {
        ProductImport importRecord = productImportMapper.selectById(Long.parseLong(importId));
        if (importRecord == null || !importRecord.getUserId().equals(userId)) {
            throw new BusinessException("导入记录不存在");
        }

        if (importRecord.getStatus() != 0) {
            throw new BusinessException("该导入任务已处理");
        }

        // 更新状态为处理中
        importRecord.setStatus(1);
        importRecord.setStartedAt(LocalDateTime.now());
        productImportMapper.updateById(importRecord);

        // 执行导入
        doImport(importRecord);

        return getImportStatus(userId, importId);
    }

    @Async
    protected void doImport(ProductImport importRecord) {
        try {
            File file = new File(importRecord.getFilePath());
            Long userId = importRecord.getUserId();

            int successCount = 0;
            int failedCount = 0;
            int skippedCount = 0;

            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            try (FileInputStream fis = new FileInputStream(file);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                    XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                    String sheetName = sheet.getSheetName();

                    // 跳过空Sheet
                    if (sheet.getPhysicalNumberOfRows() < 3) {
                        continue;
                    }

                    // 提取当前Sheet的图片，按行号索引
                    Map<Integer, String> rowImageMap = extractSheetImages(sheet, dateDir);

                    // 第一行获取品牌缩写
                    Row brandRow = sheet.getRow(0);
                    String brandCode = getCellStringValue(brandRow.getCell(0));

                    // 品牌全称从Sheet名称解析
                    String brandName = sheetName;

                    // 从第3行开始读取数据
                    for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                        Row row = sheet.getRow(rowIndex);
                        if (row == null) continue;

                        try {
                            // 读取数据
                            String xkNo = getCellStringValue(row.getCell(1));    // B列: XK NO.
                            String oeNo = getCellStringValue(row.getCell(2));    // C列: OE NO.
                            String priceStr = getCellStringValue(row.getCell(4)); // E列: 售价
                            String remark = getCellStringValue(row.getCell(5));  // F列: 备注

                            // 跳过空行
                            if (StrUtil.isBlank(xkNo) && StrUtil.isBlank(oeNo)) {
                                continue;
                            }

                            // 检查是否已存在
                            Product existing = productMapper.selectByOeNo(userId, oeNo);
                            if (existing != null) {
                                skippedCount++;
                                continue;
                            }

                            // 解析价格
                            BigDecimal[] prices = parsePrice(priceStr);

                            // 获取该行对应的图片路径
                            String imagePath = rowImageMap.get(rowIndex);

                            // 创建产品
                            Product product = new Product();
                            product.setUserId(userId);
                            product.setBrandCode(brandCode);
                            product.setBrandName(brandName);
                            product.setXkNo(xkNo);
                            product.setOeNo(oeNo);
                            product.setImagePath(imagePath);
                            product.setPriceMin(prices[0]);
                            product.setPriceMax(prices[1]);
                            product.setPriceAvg(prices[2]);
                            product.setRemark(remark);

                            productMapper.insert(product);
                            successCount++;

                        } catch (Exception e) {
                            log.warn("导入第{}行失败: {}", rowIndex + 1, e.getMessage());
                            failedCount++;
                        }
                    }
                }
            }

            // 更新导入记录
            importRecord.setSuccessCount(successCount);
            importRecord.setFailedCount(failedCount);
            importRecord.setSkippedCount(skippedCount);
            importRecord.setStatus(2);
            importRecord.setCompletedAt(LocalDateTime.now());
            productImportMapper.updateById(importRecord);

        } catch (Exception e) {
            log.error("导入失败", e);
            importRecord.setStatus(3);
            importRecord.setErrorMessage(e.getMessage());
            importRecord.setCompletedAt(LocalDateTime.now());
            productImportMapper.updateById(importRecord);
        }
    }

    /**
     * 提取Sheet中的图片，返回行号->图片路径的映射
     */
    private Map<Integer, String> extractSheetImages(XSSFSheet sheet, String dateDir) {
        Map<Integer, String> rowImageMap = new HashMap<>();

        try {
            XSSFDrawing drawing = sheet.getDrawingPatriarch();
            if (drawing == null) {
                return rowImageMap;
            }

            for (XSSFShape shape : drawing.getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = picture.getClientAnchor();
                    if (anchor == null) continue;

                    int row1 = anchor.getRow1();
                    int col1 = anchor.getCol1();

                    // 只处理D列(第3列，索引为3)的图片
                    if (col1 == 3) {
                        try {
                            XSSFPictureData pictureData = picture.getPictureData();
                            if (pictureData == null) continue;

                            byte[] data = pictureData.getData();
                            String ext = pictureData.suggestFileExtension();
                            if (StrUtil.isBlank(ext)) {
                                ext = "jpg";
                            }

                            // 保存图片
                            String savedFileName = IdUtil.fastSimpleUUID() + "." + ext;
                            Path saveDir = Paths.get(uploadPath, "products", dateDir);
                            Files.createDirectories(saveDir);
                            Path savePath = saveDir.resolve(savedFileName);
                            Files.write(savePath, data);

                            String relativePath = "/uploads/products/" + dateDir + "/" + savedFileName;
                            rowImageMap.put(row1, relativePath);
                            
                            log.debug("提取图片: Sheet={}, Row={}, Col={}, Path={}", 
                                    sheet.getSheetName(), row1, col1, relativePath);

                        } catch (Exception e) {
                            log.warn("保存图片失败: Row={}, Error={}", row1, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取Sheet图片失败: {}", e.getMessage());
        }

        log.info("Sheet [{}] 提取到 {} 张图片", sheet.getSheetName(), rowImageMap.size());
        return rowImageMap;
    }

    @Override
    public ImportStatusDTO getImportStatus(Long userId, String importId) {
        ProductImport importRecord = productImportMapper.selectById(Long.parseLong(importId));
        if (importRecord == null || !importRecord.getUserId().equals(userId)) {
            throw new BusinessException("导入记录不存在");
        }

        ImportStatusDTO status = new ImportStatusDTO();
        status.setImportId(importId);
        status.setTotalRows(importRecord.getTotalRows());
        status.setSuccessCount(importRecord.getSuccessCount());
        status.setFailedCount(importRecord.getFailedCount());
        status.setSkippedCount(importRecord.getSkippedCount());
        status.setStartedAt(importRecord.getStartedAt());
        status.setCompletedAt(importRecord.getCompletedAt());
        status.setErrorMessage(importRecord.getErrorMessage());

        switch (importRecord.getStatus()) {
            case 0 -> status.setStatus("pending");
            case 1 -> status.setStatus("processing");
            case 2 -> status.setStatus("completed");
            case 3 -> status.setStatus("failed");
        }

        // 计算进度
        if (importRecord.getTotalRows() != null && importRecord.getTotalRows() > 0) {
            int processed = (importRecord.getSuccessCount() != null ? importRecord.getSuccessCount() : 0)
                    + (importRecord.getFailedCount() != null ? importRecord.getFailedCount() : 0)
                    + (importRecord.getSkippedCount() != null ? importRecord.getSkippedCount() : 0);
            status.setProgress((int) (processed * 100.0 / importRecord.getTotalRows()));
        } else {
            status.setProgress(0);
        }

        return status;
    }

    /**
     * 解析Excel预览
     */
    private ImportResultDTO parseExcel(File file, Long userId) throws IOException {
        ImportResultDTO result = new ImportResultDTO();
        List<ImportResultDTO.BrandSheetInfo> brandSheets = new ArrayList<>();
        List<Map<String, Object>> previewData = new ArrayList<>();
        int totalProducts = 0;
        int totalImages = 0;

        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();

                if (sheet.getPhysicalNumberOfRows() < 3) {
                    continue;
                }

                ImportResultDTO.BrandSheetInfo sheetInfo = new ImportResultDTO.BrandSheetInfo();
                sheetInfo.setSheetName(sheetName);

                // 第一行获取品牌缩写
                Row brandRow = sheet.getRow(0);
                String brandCode = getCellStringValue(brandRow.getCell(0));
                sheetInfo.setBrandCode(brandCode);
                sheetInfo.setBrandName(sheetName);

                int productCount = 0;
                
                // 统计图片数量
                int imageCount = countSheetImages(sheet);

                // 统计数据行数
                for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    String xkNo = getCellStringValue(row.getCell(1));
                    String oeNo = getCellStringValue(row.getCell(2));

                    if (StrUtil.isNotBlank(xkNo) || StrUtil.isNotBlank(oeNo)) {
                        productCount++;

                        // 取前5条作为预览
                        if (previewData.size() < 5) {
                            Map<String, Object> preview = new HashMap<>();
                            preview.put("brandCode", brandCode);
                            preview.put("xkNo", xkNo);
                            preview.put("oeNo", oeNo);
                            preview.put("price", getCellStringValue(row.getCell(4)));
                            preview.put("remark", getCellStringValue(row.getCell(5)));
                            previewData.add(preview);
                        }
                    }
                }

                sheetInfo.setProductCount(productCount);
                sheetInfo.setImageCount(imageCount);
                brandSheets.add(sheetInfo);

                totalProducts += productCount;
                totalImages += imageCount;
            }
        }

        result.setBrandSheets(brandSheets);
        result.setTotalProducts(totalProducts);
        result.setTotalImages(totalImages);
        result.setPreviewData(previewData);

        return result;
    }

    /**
     * 统计Sheet中的图片数量
     */
    private int countSheetImages(XSSFSheet sheet) {
        try {
            XSSFDrawing drawing = sheet.getDrawingPatriarch();
            if (drawing == null) {
                return 0;
            }
            int count = 0;
            for (XSSFShape shape : drawing.getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = picture.getClientAnchor();
                    // 只统计D列(第3列)的图片
                    if (anchor != null && anchor.getCol1() == 3) {
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            log.warn("统计图片数量失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 解析价格
     * @return [min, max, avg]
     */
    private BigDecimal[] parsePrice(String priceStr) {
        BigDecimal[] result = new BigDecimal[3];

        if (StrUtil.isBlank(priceStr)) {
            return result;
        }

        // 尝试匹配区间价格
        Matcher rangeMatcher = PRICE_RANGE_PATTERN.matcher(priceStr);
        if (rangeMatcher.find()) {
            BigDecimal min = new BigDecimal(rangeMatcher.group(1));
            BigDecimal max = new BigDecimal(rangeMatcher.group(2));
            BigDecimal avg = min.add(max).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            result[0] = min;
            result[1] = max;
            result[2] = avg;
            return result;
        }

        // 尝试匹配单一价格
        Matcher singleMatcher = PRICE_SINGLE_PATTERN.matcher(priceStr);
        if (singleMatcher.find()) {
            BigDecimal price = new BigDecimal(singleMatcher.group(1));
            result[0] = price;
            result[1] = price;
            result[2] = price;
            return result;
        }

        return result;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        yield String.valueOf((long) value);
                    }
                    yield String.valueOf(value);
                }
                case FORMULA -> cell.getCellFormula();
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle brandStyle = createBrandStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle priceStyle = createPriceStyle(workbook);
            CellStyle exampleStyle = createExampleStyle(workbook);

            // 示例1: Mercedes-Benz (奔驰)
            createBrandSheet(workbook, "Mercedes-Benz", "MB", headerStyle, brandStyle, dataStyle, numberStyle, priceStyle, exampleStyle,
                new String[][]{
                    {"1", "MB001", "000 330 16 03", "", "125-150", "曲轴皮带轮"},
                    {"2", "MB002", "000 466 30 01", "", "85", "水泵"},
                    {"3", "MB003", "001 997 87 48", "", "220-280", "涡轮增压器"}
                }
            );

            // 示例2: Volvo (沃尔沃)
            createBrandSheet(workbook, "Volvo", "VL", headerStyle, brandStyle, dataStyle, numberStyle, priceStyle, exampleStyle,
                new String[][]{
                    {"1", "VL001", "20998367", "", "180", "发动机支架"},
                    {"2", "VL002", "21707133", "", "95-120", "空气滤清器"},
                    {"3", "VL003", "85000527", "", "350", "启动马达"}
                }
            );

            // 示例3: Scania (斯堪尼亚)
            createBrandSheet(workbook, "Scania", "SC", headerStyle, brandStyle, dataStyle, numberStyle, priceStyle, exampleStyle,
                new String[][]{
                    {"1", "SC001", "1452392", "", "140-170", "机油滤清器"},
                    {"2", "SC002", "1504550", "", "200", "燃油泵"},
                    {"3", "SC003", "2343574", "", "280-320", "离合器总成"}
                }
            );

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = "产品导入模板_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + 
                java.net.URLEncoder.encode(fileName, "UTF-8") + "\"");

            // 写入响应
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            log.error("生成模板失败", e);
            throw new BusinessException("生成模板失败: " + e.getMessage());
        }
    }

    /**
     * 创建品牌Sheet
     */
    private void createBrandSheet(XSSFWorkbook workbook, String brandName, String brandCode,
                                  CellStyle headerStyle, CellStyle brandStyle, CellStyle dataStyle,
                                  CellStyle numberStyle, CellStyle priceStyle, CellStyle exampleStyle,
                                  String[][] sampleData) {
        XSSFSheet sheet = workbook.createSheet(brandName);

        // 设置列宽
        sheet.setColumnWidth(0, 10 * 256);  // NO. 列
        sheet.setColumnWidth(1, 18 * 256);  // XK NO. 列
        sheet.setColumnWidth(2, 20 * 256);  // OE NO. 列
        sheet.setColumnWidth(3, 15 * 256);  // PICTURE 列
        sheet.setColumnWidth(4, 15 * 256);  // 售价 列
        sheet.setColumnWidth(5, 25 * 256);  // 备注 列

        // 第1行: 品牌缩写
        Row brandRow = sheet.createRow(0);
        brandRow.setHeightInPoints(30);
        Cell brandCell = brandRow.createCell(0);
        brandCell.setCellValue(brandCode);
        brandCell.setCellStyle(brandStyle);

        // 合并单元格显示说明
        Cell brandInfoCell = brandRow.createCell(1);
        brandInfoCell.setCellValue("← 品牌缩写（必填）");
        brandInfoCell.setCellStyle(exampleStyle);

        // 第2行: 表头
        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(35);
        String[] headers = {"NO.", "XK NO.", "OE NO.", "PICTURE", "售价", "备注"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 第3行起: 示例数据
        for (int i = 0; i < sampleData.length; i++) {
            Row dataRow = sheet.createRow(2 + i);
            dataRow.setHeightInPoints(25);
            String[] rowData = sampleData[i];
            
            for (int j = 0; j < rowData.length; j++) {
                Cell cell = dataRow.createCell(j);
                cell.setCellValue(rowData[j]);
                if (j == 0) {
                    cell.setCellStyle(numberStyle);
                } else if (j == 4) {
                    cell.setCellStyle(priceStyle);
                } else {
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // 添加说明行
        int lastRow = 2 + sampleData.length + 1;
        Row noteRow = sheet.createRow(lastRow);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("💡 提示：删除此行及以上示例数据，填入您的产品数据。支持批量粘贴。");
        noteCell.setCellStyle(exampleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(lastRow, lastRow, 0, 5));
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        
        // 背景色 - 深蓝色
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte)37, (byte)99, (byte)235}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 字体
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Arial");
        style.setFont(font);
        
        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }

    /**
     * 创建品牌行样式
     */
    private CellStyle createBrandStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        
        // 背景色 - 浅蓝色
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte)219, (byte)234, (byte)254}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 字体
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(new byte[]{(byte)37, (byte)99, (byte)235}, null));
        style.setFont(font);
        
        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 边框
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        
        return style;
    }

    /**
     * 创建数据行样式
     */
    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 字体
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        style.setFont(font);
        
        // 对齐
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        
        return style;
    }

    /**
     * 创建序号列样式
     */
    private CellStyle createNumberStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 背景色 - 浅灰色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 字体
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setBold(true);
        style.setFont(font);
        
        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }

    /**
     * 创建价格列样式
     */
    private CellStyle createPriceStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        
        // 背景色 - 浅绿色
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte)220, (byte)252, (byte)231}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 字体
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(new byte[]{(byte)22, (byte)163, (byte)74}, null));
        style.setFont(font);
        
        // 对齐
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }

    /**
     * 创建说明样式
     */
    private CellStyle createExampleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        
        // 背景色 - 浅黄色
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte)254, (byte)249, (byte)195}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 字体
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setItalic(true);
        font.setColor(new XSSFColor(new byte[]{(byte)161, (byte)98, (byte)7}, null));
        style.setFont(font);
        
        // 对齐
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
}

