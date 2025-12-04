package com.trucktools.customer.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.customer.dto.*;
import com.trucktools.customer.entity.Customer;
import com.trucktools.customer.entity.CustomerImport;
import com.trucktools.customer.mapper.CustomerImportMapper;
import com.trucktools.customer.mapper.CustomerMapper;
import com.trucktools.customer.service.CustomerImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 客户导入服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerImportServiceImpl implements CustomerImportService {

    private final CustomerImportMapper customerImportMapper;
    private final CustomerMapper customerMapper;

    // 缓存解析后的数据（实际生产环境应使用Redis）
    private final Map<String, List<Map<String, String>>> parsedDataCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> headersCache = new ConcurrentHashMap<>();
    private final Map<String, ImportValidateRequest> validateRequestCache = new ConcurrentHashMap<>();

    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // 系统字段与中文列名的映射关系
    private static final Map<String, List<String>> FIELD_ALIASES = new LinkedHashMap<>();
    static {
        FIELD_ALIASES.put("name", List.of("客户姓名", "姓名", "名称", "name", "客户名称", "联系人"));
        FIELD_ALIASES.put("email", List.of("邮箱", "电子邮件", "email", "e-mail", "邮件地址"));
        FIELD_ALIASES.put("phone", List.of("手机号", "电话", "手机", "phone", "电话号码", "联系电话", "mobile"));
        FIELD_ALIASES.put("company", List.of("公司", "所属公司", "公司名称", "company", "企业", "单位"));
        FIELD_ALIASES.put("position", List.of("职位", "职务", "position", "title", "岗位"));
        FIELD_ALIASES.put("country", List.of("国家", "country", "国家/地区", "地区"));
        FIELD_ALIASES.put("website", List.of("官网", "公司官网", "网站", "website", "网址"));
        FIELD_ALIASES.put("address", List.of("地址", "公司地址", "address", "联系地址"));
        FIELD_ALIASES.put("wechatName", List.of("微信", "微信名称", "微信ID", "微信号", "wechat", "weixin", "微信名称/ID"));
        FIELD_ALIASES.put("whatsappName", List.of("whatsapp", "whatsapp名称", "whatsapp号码", "whatsapp名称/号码", "wa"));
        FIELD_ALIASES.put("meetingTime", List.of("会面时间", "见面时间", "会议时间"));
        FIELD_ALIASES.put("meetingLocation", List.of("会面地点", "见面地点", "会议地点"));
        FIELD_ALIASES.put("followUpStatus", List.of("跟进状态", "状态", "follow up status", "follow-up status"));
        FIELD_ALIASES.put("priority", List.of("优先级", "重要程度", "priority"));
        FIELD_ALIASES.put("remark", List.of("备注", "备注信息", "remark", "notes", "说明"));
    }

    @Override
    public ImportUploadResult uploadAndParse(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            throw new BusinessException("仅支持 .xlsx 格式的Excel文件");
        }

        // 生成导入ID
        String importId = "import_" + System.currentTimeMillis() + "_" + userId;

        // 解析Excel
        List<Map<String, String>> dataList = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), new ReadListener<Map<Integer, String>>() {
                private Map<Integer, String> headerMap = new LinkedHashMap<>();

                @Override
                public void invokeHead(Map<Integer, com.alibaba.excel.metadata.data.ReadCellData<?>> headMap, AnalysisContext context) {
                    // 获取表头
                    for (Map.Entry<Integer, com.alibaba.excel.metadata.data.ReadCellData<?>> entry : headMap.entrySet()) {
                        String headerValue = entry.getValue().getStringValue();
                        if (StrUtil.isNotBlank(headerValue)) {
                            headerMap.put(entry.getKey(), headerValue.trim());
                            headers.add(headerValue.trim());
                        }
                    }
                    log.info("解析到表头: {}", headers);
                }

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    // 数据行
                    Map<String, String> rowData = new LinkedHashMap<>();
                    for (Map.Entry<Integer, String> entry : headerMap.entrySet()) {
                        String headerName = entry.getValue();
                        if (StrUtil.isNotBlank(headerName)) {
                            String cellValue = data.get(entry.getKey());
                            rowData.put(headerName, cellValue != null ? cellValue.trim() : "");
                        }
                    }
                    // 跳过完全空的行
                    if (rowData.values().stream().anyMatch(StrUtil::isNotBlank)) {
                        dataList.add(rowData);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel解析完成，共 {} 行数据", dataList.size());
                }
            }).sheet().doRead();

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }

        if (headers.isEmpty()) {
            throw new BusinessException("Excel文件为空或格式不正确");
        }

        if (dataList.isEmpty()) {
            throw new BusinessException("Excel文件中没有数据");
        }

        if (dataList.size() > 10000) {
            throw new BusinessException("单次最多导入10000条数据，当前文件包含 " + dataList.size() + " 条");
        }

        // 缓存解析数据
        parsedDataCache.put(importId, dataList);
        headersCache.put(importId, headers);

        // 创建导入记录
        CustomerImport importRecord = new CustomerImport();
        importRecord.setUserId(userId);
        importRecord.setFileName(originalFilename);
        importRecord.setFileUrl("memory://" + importId); // 数据暂存内存，不上传OSS
        importRecord.setFileSize(file.getSize());
        importRecord.setTotalRows(dataList.size());
        importRecord.setSuccessCount(0);
        importRecord.setFailedCount(0);
        importRecord.setSkippedCount(0);
        importRecord.setStatus(0); // 待处理
        customerImportMapper.insert(importRecord);

        // 智能匹配字段映射
        Map<String, String> suggestedMapping = suggestFieldMapping(headers);

        // 预览数据（前5行）
        List<Map<String, String>> previewData = dataList.stream()
                .limit(5)
                .collect(Collectors.toList());

        return ImportUploadResult.builder()
                .importId(importId)
                .fileName(originalFilename)
                .totalRows(dataList.size())
                .headers(headers)
                .suggestedMapping(suggestedMapping)
                .previewData(previewData)
                .build();
    }

    @Override
    public ImportValidationResult validate(Long userId, String importId, ImportValidateRequest request) {
        List<Map<String, String>> dataList = parsedDataCache.get(importId);
        if (dataList == null) {
            throw new BusinessException("导入任务不存在或已过期，请重新上传文件");
        }

        Map<String, String> fieldMapping = request.getFieldMapping();
        if (fieldMapping == null || fieldMapping.isEmpty()) {
            throw new BusinessException("请配置字段映射关系");
        }

        // 验证必填字段是否有映射（只要求姓名必填）
        boolean hasNameMapping = fieldMapping.containsValue("name");
        if (!hasNameMapping) {
            throw new BusinessException("客户姓名为必填字段，请确保已映射");
        }

        // 缓存验证请求
        validateRequestCache.put(importId, request);

        // 验证数据
        List<ImportValidationResult.ImportError> errors = new ArrayList<>();
        Set<String> existingCustomerKeys = getExistingCustomerKeys(userId); // 姓名+邮箱+国家
        Set<String> keysInFile = new HashSet<>(); // 文件内已出现的组合
        int validRows = 0;
        int invalidRows = 0;
        int duplicateRows = 0;
        int duplicateInFile = 0; // 文件内重复
        int duplicateInDb = 0; // 数据库重复

        // 反转映射：系统字段 -> Excel列名
        Map<String, String> reverseMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            if (StrUtil.isNotBlank(entry.getValue())) {
                reverseMapping.put(entry.getValue(), entry.getKey());
            }
        }

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, String> row = dataList.get(i);
            int rowNum = i + 2; // Excel行号（加上表头行和1-based索引）
            boolean rowValid = true;
            boolean isDuplicate = false;

            // 获取映射后的值
            String nameColumn = reverseMapping.get("name");
            String emailColumn = reverseMapping.get("email");
            String phoneColumn = reverseMapping.get("phone");
            String companyColumn = reverseMapping.get("company");
            String wechatNameColumn = reverseMapping.get("wechatName");
            String whatsappNameColumn = reverseMapping.get("whatsappName");

            String name = nameColumn != null ? row.get(nameColumn) : null;
            String email = emailColumn != null ? row.get(emailColumn) : null;
            String phone = phoneColumn != null ? row.get(phoneColumn) : null;
            String company = companyColumn != null ? row.get(companyColumn) : null;
            String wechatName = wechatNameColumn != null ? row.get(wechatNameColumn) : null;
            String whatsappName = whatsappNameColumn != null ? row.get(whatsappNameColumn) : null;

            // 验证姓名
            if (StrUtil.isBlank(name)) {
                errors.add(ImportValidationResult.ImportError.builder()
                        .row(rowNum)
                        .field("name")
                        .value(name)
                        .message("客户姓名不能为空")
                        .build());
                rowValid = false;
            }

            // 验证邮箱（允许为空，但如果不为空则检查格式）
            if (StrUtil.isNotBlank(email)) {
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    errors.add(ImportValidationResult.ImportError.builder()
                            .row(rowNum)
                            .field("email")
                            .value(email)
                            .message("邮箱格式错误")
                            .build());
                    rowValid = false;
                }
            }
            
            // 验证至少有一种联系方式或公司信息
            boolean hasContactInfo = StrUtil.isNotBlank(email) 
                    || StrUtil.isNotBlank(phone) 
                    || StrUtil.isNotBlank(company)
                    || StrUtil.isNotBlank(wechatName) 
                    || StrUtil.isNotBlank(whatsappName);
            
            if (!hasContactInfo) {
                errors.add(ImportValidationResult.ImportError.builder()
                        .row(rowNum)
                        .field("联系方式")
                        .value("")
                        .message("至少需要填写一种联系方式（邮箱、手机号、微信、WhatsApp）或公司名称")
                        .build());
                rowValid = false;
            }
            
            // 检查重复（只有当邮箱不为空时才进行重复检查）
            if (StrUtil.isNotBlank(email)) {
                // 获取国家字段
                String countryColumn = reverseMapping.get("country");
                String country = countryColumn != null ? row.get(countryColumn) : "";
                
                // 生成唯一键：姓名+邮箱+国家（全部转小写比较）
                String customerKey = buildCustomerKey(name, email, country);
                
                // 检查文件内重复
                if (keysInFile.contains(customerKey)) {
                    errors.add(ImportValidationResult.ImportError.builder()
                            .row(rowNum)
                            .field("name+email+country")
                            .value(name + " / " + email + " / " + country)
                            .message("文件内客户重复（姓名+邮箱+国家一致），将跳过此行")
                            .build());
                    isDuplicate = true;
                    duplicateInFile++;
                }
                
                // 检查数据库重复
                if (!keysInFile.contains(customerKey) && existingCustomerKeys.contains(customerKey)) {
                    String action = "overwrite".equals(request.getImportMode()) ? "将覆盖更新" : 
                                  "merge".equals(request.getImportMode()) ? "将合并更新" : "将作为新客户添加";
                    errors.add(ImportValidationResult.ImportError.builder()
                            .row(rowNum)
                            .field("name+email+country")
                            .value(name + " / " + email + " / " + country)
                            .message("客户已存在（姓名+邮箱+国家一致），" + action)
                            .build());
                    isDuplicate = true;
                    duplicateInDb++;
                }
                
                keysInFile.add(customerKey);
            }

            // 验证优先级
            String priorityColumn = reverseMapping.get("priority");
            if (priorityColumn != null) {
                String priorityStr = row.get(priorityColumn);
                if (StrUtil.isNotBlank(priorityStr)) {
                    try {
                        int priority = Integer.parseInt(priorityStr.replaceAll("[^0-9]", ""));
                        if (priority < 1 || priority > 3) {
                            errors.add(ImportValidationResult.ImportError.builder()
                                    .row(rowNum)
                                    .field("priority")
                                    .value(priorityStr)
                                    .message("优先级必须是1-3之间的数字（1=高，2=中，3=低）")
                                    .build());
                        }
                    } catch (NumberFormatException e) {
                        errors.add(ImportValidationResult.ImportError.builder()
                                .row(rowNum)
                                .field("priority")
                                .value(priorityStr)
                                .message("优先级必须是数字")
                                .build());
                    }
                }
            }

            // 验证会面时间
            String meetingTimeColumn = reverseMapping.get("meetingTime");
            if (meetingTimeColumn != null) {
                String meetingTimeStr = row.get(meetingTimeColumn);
                if (StrUtil.isNotBlank(meetingTimeStr)) {
                    if (!isValidDateTime(meetingTimeStr)) {
                        errors.add(ImportValidationResult.ImportError.builder()
                                .row(rowNum)
                                .field("meetingTime")
                                .value(meetingTimeStr)
                                .message("日期时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式")
                                .build());
                    }
                }
            }

            if (rowValid) {
                validRows++;
                if (isDuplicate) {
                    duplicateRows++; // 重复数据仍算有效，只是额外标记
                }
            } else {
                invalidRows++;
            }
        }

        // 更新导入记录状态
        CustomerImport importRecord = getImportRecord(importId);
        if (importRecord != null) {
            importRecord.setFieldMapping(fieldMapping);
            importRecord.setImportMode(request.getImportMode());
            customerImportMapper.updateById(importRecord);
        }

        return ImportValidationResult.builder()
                .totalRows(dataList.size())
                .validRows(validRows)
                .invalidRows(invalidRows)
                .duplicateRows(duplicateRows)
                .duplicateInFile(duplicateInFile)
                .duplicateInDb(duplicateInDb)
                .errors(errors)
                .build();
    }

    @Override
    @Transactional
    public void execute(Long userId, String importId, ImportExecuteRequest request) {
        List<Map<String, String>> dataList = parsedDataCache.get(importId);
        ImportValidateRequest validateRequest = validateRequestCache.get(importId);

        if (dataList == null || validateRequest == null) {
            throw new BusinessException("导入任务不存在或已过期，请重新上传文件");
        }

        CustomerImport importRecord = getImportRecord(importId);
        if (importRecord == null) {
            throw new BusinessException("导入记录不存在");
        }

        // 更新状态为处理中
        importRecord.setStatus(1);
        importRecord.setStartedAt(LocalDateTime.now());
        customerImportMapper.updateById(importRecord);

        Map<String, String> fieldMapping = validateRequest.getFieldMapping();
        String importMode = request.getImportMode() != null ? request.getImportMode() : "append";

        // 反转映射
        Map<String, String> reverseMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            if (StrUtil.isNotBlank(entry.getValue())) {
                reverseMapping.put(entry.getValue(), entry.getKey());
            }
        }

        Set<String> existingCustomerKeysInDb = getExistingCustomerKeys(userId);
        Set<String> importedKeys = new HashSet<>(); // 本次导入中已处理的客户键
        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;

        for (Map<String, String> row : dataList) {
            try {
                String name = getFieldValue(row, reverseMapping, "name");
                String email = getFieldValue(row, reverseMapping, "email");
                String phone = getFieldValue(row, reverseMapping, "phone");
                String company = getFieldValue(row, reverseMapping, "company");
                String wechatName = getFieldValue(row, reverseMapping, "wechatName");
                String whatsappName = getFieldValue(row, reverseMapping, "whatsappName");
                String country = getFieldValue(row, reverseMapping, "country");

                // 验证姓名必填
                if (StrUtil.isBlank(name)) {
                    failedCount++;
                    continue;
                }

                // 验证至少有一种联系方式或公司信息
                boolean hasContactInfo = StrUtil.isNotBlank(email) 
                        || StrUtil.isNotBlank(phone) 
                        || StrUtil.isNotBlank(company)
                        || StrUtil.isNotBlank(wechatName) 
                        || StrUtil.isNotBlank(whatsappName);
                
                if (!hasContactInfo) {
                    failedCount++;
                    continue;
                }

                // 如果邮箱不为空，验证格式
                if (StrUtil.isNotBlank(email) && !EMAIL_PATTERN.matcher(email).matches()) {
                    failedCount++;
                    continue;
                }

                // 重复检查（只有当邮箱不为空时才进行）
                // 如果邮箱为空，无法准确判断重复，每条数据都作为新客户导入
                if (StrUtil.isNotBlank(email)) {
                    // 生成唯一键：姓名+邮箱+国家
                    String customerKey = buildCustomerKey(name, email, country);
                    boolean existsInDb = existingCustomerKeysInDb.contains(customerKey);
                    boolean existsInCurrentImport = importedKeys.contains(customerKey);

                    if (existsInDb) {
                        if ("overwrite".equals(importMode) || "merge".equals(importMode)) {
                            // 覆盖或合并模式：更新现有客户
                            Customer existing = findByCustomerKey(userId, name, email, country);
                            if (existing != null) {
                                updateCustomerFromRow(existing, row, reverseMapping, "merge".equals(importMode));
                                customerMapper.updateById(existing);
                                successCount++;
                                importedKeys.add(customerKey);
                                continue;
                            }
                        }
                        // append 模式：仍然创建新客户（允许重复）
                    }

                    // 文件内重复：跳过，只导入第一条
                    if (existsInCurrentImport) {
                        skippedCount++;
                        continue;
                    }
                    
                    // 记录已导入的客户键
                    importedKeys.add(customerKey);
                }

                // 创建新客户
                Customer customer = createCustomerFromRow(userId, row, reverseMapping, importId);
                customerMapper.insert(customer);
                successCount++;

            } catch (Exception e) {
                log.error("导入行数据失败: {}", row, e);
                failedCount++;
            }
        }

        // 更新导入记录
        importRecord.setStatus(2); // 完成
        importRecord.setSuccessCount(successCount);
        importRecord.setFailedCount(failedCount);
        importRecord.setSkippedCount(skippedCount);
        importRecord.setCompletedAt(LocalDateTime.now());
        customerImportMapper.updateById(importRecord);

        // 清理缓存
        parsedDataCache.remove(importId);
        headersCache.remove(importId);
        validateRequestCache.remove(importId);

        log.info("导入完成: importId={}, success={}, failed={}, skipped={}",
                importId, successCount, failedCount, skippedCount);
    }

    @Override
    public ImportStatusResult getStatus(Long userId, String importId) {
        CustomerImport importRecord = getImportRecord(importId);
        if (importRecord == null) {
            throw new BusinessException("导入任务不存在");
        }

        if (!importRecord.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此导入任务");
        }

        String status;
        int progress = 0;
        switch (importRecord.getStatus()) {
            case 0:
                status = "pending";
                break;
            case 1:
                status = "processing";
                progress = 50; // 处理中显示50%
                break;
            case 2:
                status = "completed";
                progress = 100;
                break;
            case 3:
                status = "failed";
                break;
            default:
                status = "unknown";
        }

        return ImportStatusResult.builder()
                .importId(importId)
                .status(status)
                .totalRows(importRecord.getTotalRows())
                .successCount(importRecord.getSuccessCount() != null ? importRecord.getSuccessCount() : 0)
                .failedCount(importRecord.getFailedCount() != null ? importRecord.getFailedCount() : 0)
                .skippedCount(importRecord.getSkippedCount() != null ? importRecord.getSkippedCount() : 0)
                .progress(progress)
                .startedAt(importRecord.getStartedAt())
                .completedAt(importRecord.getCompletedAt())
                .build();
    }

    // ===== 私有辅助方法 =====

    /**
     * 智能匹配字段映射
     */
    private Map<String, String> suggestFieldMapping(List<String> headers) {
        Map<String, String> mapping = new LinkedHashMap<>();
        
        for (String header : headers) {
            String normalizedHeader = header.toLowerCase().trim();
            for (Map.Entry<String, List<String>> entry : FIELD_ALIASES.entrySet()) {
                String fieldName = entry.getKey();
                List<String> aliases = entry.getValue();
                
                for (String alias : aliases) {
                    if (normalizedHeader.equals(alias.toLowerCase()) ||
                        normalizedHeader.contains(alias.toLowerCase())) {
                        mapping.put(header, fieldName);
                        break;
                    }
                }
                if (mapping.containsKey(header)) {
                    break;
                }
            }
        }
        
        return mapping;
    }

    /**
     * 构建客户唯一键（姓名+邮箱+国家）
     */
    private String buildCustomerKey(String name, String email, String country) {
        String n = StrUtil.isNotBlank(name) ? name.toLowerCase().trim() : "";
        String e = StrUtil.isNotBlank(email) ? email.toLowerCase().trim() : "";
        String c = StrUtil.isNotBlank(country) ? country.toLowerCase().trim() : "";
        return n + "|" + e + "|" + c;
    }

    /**
     * 获取用户已有的所有客户唯一键（姓名+邮箱+国家）
     */
    private Set<String> getExistingCustomerKeys(Long userId) {
        List<Customer> customers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .select(Customer::getName, Customer::getEmail, Customer::getCountry)
        );
        return customers.stream()
                .map(c -> buildCustomerKey(c.getName(), c.getEmail(), c.getCountry()))
                .collect(Collectors.toSet());
    }

    /**
     * 根据姓名+邮箱+国家查找客户
     */
    private Customer findByCustomerKey(Long userId, String name, String email, String country) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<Customer>()
                .eq(Customer::getUserId, userId)
                .eq(Customer::getName, name);
        
        // 处理邮箱（可能为空）
        if (StrUtil.isNotBlank(email)) {
            wrapper.eq(Customer::getEmail, email);
        } else {
            wrapper.and(w -> w.isNull(Customer::getEmail).or().eq(Customer::getEmail, ""));
        }
        
        // 处理国家（可能为空）
        if (StrUtil.isNotBlank(country)) {
            wrapper.eq(Customer::getCountry, country);
        } else {
            wrapper.and(w -> w.isNull(Customer::getCountry).or().eq(Customer::getCountry, ""));
        }
        
        return customerMapper.selectOne(wrapper);
    }

    /**
     * 获取导入记录
     */
    private CustomerImport getImportRecord(String importId) {
        // 从importId中提取用户ID来构建查询
        // importId格式: import_{timestamp}_{userId}
        try {
            String[] parts = importId.split("_");
            if (parts.length >= 3) {
                Long userId = Long.parseLong(parts[2]);
                
                // 根据 fileUrl 中的 importId 精确查找
                return customerImportMapper.selectOne(
                        new LambdaQueryWrapper<CustomerImport>()
                                .eq(CustomerImport::getUserId, userId)
                                .eq(CustomerImport::getFileUrl, "memory://" + importId)
                                .last("LIMIT 1")
                );
            }
        } catch (Exception e) {
            log.warn("解析importId失败: {}", importId);
        }
        return null;
    }

    /**
     * 获取字段值
     */
    private String getFieldValue(Map<String, String> row, Map<String, String> reverseMapping, String fieldName) {
        String column = reverseMapping.get(fieldName);
        if (column != null) {
            return row.get(column);
        }
        return null;
    }

    /**
     * 从Excel行数据创建客户
     */
    private Customer createCustomerFromRow(Long userId, Map<String, String> row, 
                                           Map<String, String> reverseMapping, String importId) {
        Customer customer = new Customer();
        customer.setUserId(userId);
        customer.setSource("import");
        customer.setSourceFile(importId);
        customer.setEmailStatus(1); // 默认有效
        customer.setEmailCount(0);
        customer.setPriority(2); // 默认优先级（中）

        setCustomerFieldsFromRow(customer, row, reverseMapping, false);

        return customer;
    }

    /**
     * 更新客户信息
     */
    private void updateCustomerFromRow(Customer customer, Map<String, String> row,
                                       Map<String, String> reverseMapping, boolean mergeMode) {
        setCustomerFieldsFromRow(customer, row, reverseMapping, mergeMode);
    }

    /**
     * 设置客户字段
     */
    private void setCustomerFieldsFromRow(Customer customer, Map<String, String> row,
                                          Map<String, String> reverseMapping, boolean mergeMode) {
        String name = getFieldValue(row, reverseMapping, "name");
        String email = getFieldValue(row, reverseMapping, "email");
        String phone = getFieldValue(row, reverseMapping, "phone");
        String company = getFieldValue(row, reverseMapping, "company");
        String position = getFieldValue(row, reverseMapping, "position");
        String country = getFieldValue(row, reverseMapping, "country");
        String website = getFieldValue(row, reverseMapping, "website");
        String address = getFieldValue(row, reverseMapping, "address");
        String meetingTime = getFieldValue(row, reverseMapping, "meetingTime");
        String meetingLocation = getFieldValue(row, reverseMapping, "meetingLocation");
        String priorityStr = getFieldValue(row, reverseMapping, "priority");
        String remark = getFieldValue(row, reverseMapping, "remark");

        // 设置必填字段
        if (StrUtil.isNotBlank(name)) {
            customer.setName(name);
        }
        if (StrUtil.isNotBlank(email)) {
            customer.setEmail(email);
        }

        // 设置可选字段（mergeMode时只更新非空值）
        if (StrUtil.isNotBlank(phone) || !mergeMode) {
            customer.setPhone(phone);
        }
        if (StrUtil.isNotBlank(company) || !mergeMode) {
            customer.setCompany(company);
        }
        if (StrUtil.isNotBlank(position) || !mergeMode) {
            customer.setPosition(position);
        }
        if (StrUtil.isNotBlank(country) || !mergeMode) {
            customer.setCountry(country);
        }
        if (StrUtil.isNotBlank(website) || !mergeMode) {
            customer.setWebsite(website);
        }
        if (StrUtil.isNotBlank(address) || !mergeMode) {
            customer.setAddress(address);
        }
        if (StrUtil.isNotBlank(meetingLocation) || !mergeMode) {
            customer.setMeetingLocation(meetingLocation);
        }
        if (StrUtil.isNotBlank(remark) || !mergeMode) {
            customer.setRemark(remark);
        }

        // 解析优先级（1=高，2=中，3=低）
        if (StrUtil.isNotBlank(priorityStr)) {
            try {
                int priority = Integer.parseInt(priorityStr.replaceAll("[^0-9]", ""));
                if (priority >= 1 && priority <= 3) {
                    customer.setPriority(priority);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // 解析会面时间
        if (StrUtil.isNotBlank(meetingTime)) {
            LocalDateTime dateTime = parseDateTime(meetingTime);
            if (dateTime != null) {
                customer.setMeetingTime(dateTime);
            }
        }
    }

    /**
     * 验证日期时间格式
     */
    private boolean isValidDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr) != null;
    }

    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                if (dateTimeStr.length() <= 10) {
                    // 只有日期，补上时间
                    return LocalDateTime.parse(dateTimeStr + " 00:00:00", 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}

