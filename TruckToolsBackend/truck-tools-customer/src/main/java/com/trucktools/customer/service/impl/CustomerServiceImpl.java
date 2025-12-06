package com.trucktools.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.customer.dto.CustomerQueryParam;
import com.trucktools.customer.dto.CustomerRequest;
import com.trucktools.customer.dto.CustomerVO;
import com.trucktools.customer.entity.Customer;
import com.trucktools.customer.entity.CustomerEvent;
import com.trucktools.customer.mapper.CustomerEventMapper;
import com.trucktools.customer.mapper.CustomerMapper;
import com.trucktools.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 客户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerEventMapper customerEventMapper;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public PageResult<CustomerVO> getPage(CustomerQueryParam param) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getUserId, param.getUserId());
        
        // 关键词搜索
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.and(w -> w
                    .like(Customer::getName, param.getKeyword())
                    .or().like(Customer::getCompany, param.getKeyword())
                    .or().like(Customer::getEmail, param.getKeyword()));
        }
        
        // 条件筛选
        if (param.getPriority() != null) {
            wrapper.eq(Customer::getPriority, param.getPriority());
        }
        // 多选优先级筛选
        if (StringUtils.hasText(param.getPriorities())) {
            String[] priorityArr = param.getPriorities().split(",");
            List<Integer> priorityList = java.util.Arrays.stream(priorityArr)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            wrapper.in(Customer::getPriority, priorityList);
        }
        if (StringUtils.hasText(param.getCountry())) {
            wrapper.eq(Customer::getCountry, param.getCountry());
        }
        // 多选国家筛选
        if (StringUtils.hasText(param.getCountries())) {
            String[] countryArr = param.getCountries().split(",");
            List<String> countryList = java.util.Arrays.stream(countryArr)
                    .map(String::trim)
                    .collect(Collectors.toList());
            wrapper.in(Customer::getCountry, countryList);
        }
        if (StringUtils.hasText(param.getSource())) {
            wrapper.eq(Customer::getSource, param.getSource());
        }
        
        // 排序
        String sortField = param.getSortField();
        if (StringUtils.hasText(sortField)) {
            boolean isAsc = param.isAsc();
            switch (sortField) {
                case "priority" -> wrapper.orderBy(true, isAsc, Customer::getPriority);
                case "createdAt" -> wrapper.orderBy(true, isAsc, Customer::getCreatedAt);
                case "meetingTime" -> wrapper.orderBy(true, isAsc, Customer::getMeetingTime);
                case "name" -> wrapper.orderBy(true, isAsc, Customer::getName);
                default -> wrapper.orderByDesc(Customer::getPriority).orderByDesc(Customer::getCreatedAt);
            }
        } else {
            wrapper.orderByAsc(Customer::getPriority).orderByDesc(Customer::getCreatedAt);
        }
        
        Page<Customer> page = new Page<>(param.getPage(), param.getPageSize());
        Page<Customer> result = customerMapper.selectPage(page, wrapper);
        
        List<CustomerVO> list = result.getRecords().stream()
                .map(customer -> {
                    CustomerVO vo = convertToVO(customer);
                    // 填充最新事件
                    CustomerEvent latestEvent = customerEventMapper.selectLatestByCustomerId(customer.getId());
                    if (latestEvent != null) {
                        vo.setLatestEventContent(latestEvent.getEventContent());
                        vo.setLatestEventTime(latestEvent.getEventTime());
                        vo.setLatestEventType(latestEvent.getEventStatus());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        return PageResult.of(list, result.getTotal(), param.getPage(), param.getPageSize());
    }

    @Override
    public CustomerVO getDetail(Long userId, Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }
        return convertToVO(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, CustomerRequest request) {
        // 检查邮箱是否存在（仅当邮箱不为空时检查）
        if (StrUtil.isNotBlank(request.getEmail())) {
            if (customerMapper.existsByEmail(userId, request.getEmail()) > 0) {
                throw new BusinessException(ResultCode.CUSTOMER_EMAIL_EXISTS);
            }
        }
        
        Customer customer = new Customer();
        copyFromRequest(customer, request);
        customer.setUserId(userId);
        customer.setSource("manual");
        customer.setEmailStatus(1);
        customer.setEmailCount(0);
        
        customerMapper.insert(customer);
        log.info("创建客户: userId={}, customerId={}", userId, customer.getId());
        return customer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long customerId, CustomerRequest request) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }
        
        // 如果修改了邮箱，检查是否重复
        String oldEmail = customer.getEmail();
        String newEmail = request.getEmail();
        
        // 只有当邮箱发生变化时才检查重复
        boolean emailChanged = !java.util.Objects.equals(oldEmail, newEmail);
        if (emailChanged && StrUtil.isNotBlank(newEmail)) {
            if (customerMapper.existsByEmail(userId, newEmail) > 0) {
                throw new BusinessException(ResultCode.CUSTOMER_EMAIL_EXISTS);
            }
        }
        
        copyFromRequest(customer, request);
        customerMapper.updateById(customer);
        log.info("更新客户: customerId={}", customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }
        
        customerMapper.deleteById(customerId);
        log.info("删除客户: customerId={}", customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(Long userId, List<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return;
        }
        
        customerMapper.delete(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getUserId, userId)
                .in(Customer::getId, customerIds));
        log.info("批量删除客户: userId={}, count={}", userId, customerIds.size());
    }

    @Override
    public String export(Long userId, List<Long> ids, Map<String, Object> filter, List<String> fields) {
        // TODO: 实现Excel导出功能
        return "https://example.com/exports/customers.xlsx";
    }

    @Override
    public Customer getById(Long customerId) {
        return customerMapper.selectById(customerId);
    }

    @Override
    public List<Customer> getByIds(List<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return List.of();
        }
        return customerMapper.selectBatchIds(customerIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmailInfo(Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer != null) {
            customer.setLastEmailTime(LocalDateTime.now());
            customer.setEmailCount(customer.getEmailCount() + 1);
            customerMapper.updateById(customer);
        }
    }

    /**
     * 从请求复制属性
     */
    private void copyFromRequest(Customer customer, CustomerRequest request) {
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCompany(request.getCompany());
        customer.setPosition(request.getPosition());
        customer.setCountry(request.getCountry());
        customer.setAddress(request.getAddress());
        customer.setWebsite(request.getWebsite());
        customer.setPriority(request.getPriority() != null ? request.getPriority() : 2);
        
        // 处理会面时间（前端发送的是日期字符串 YYYY-MM-DD）
        if (StrUtil.isNotBlank(request.getMeetingTime())) {
            try {
                LocalDateTime meetingTime = LocalDate.parse(request.getMeetingTime()).atStartOfDay();
                customer.setMeetingTime(meetingTime);
            } catch (Exception e) {
                // 如果解析失败，设置为null
                customer.setMeetingTime(null);
            }
        } else {
            customer.setMeetingTime(null);
        }
        
        customer.setMeetingLocation(request.getMeetingLocation());
        customer.setWechatName(request.getWechatName());
        customer.setWechatQrcode(request.getWechatQrcode());
        customer.setWhatsappName(request.getWhatsappName());
        customer.setWhatsappQrcode(request.getWhatsappQrcode());
        customer.setFollowUpStatus(request.getFollowUpStatus() != null ? request.getFollowUpStatus() : "pending_us");
        customer.setRemark(request.getRemark());
        
        if (request.getCustomFields() != null) {
            Map<String, Object> extraData = new HashMap<>(request.getCustomFields());
            customer.setExtraData(extraData);
        }
    }

    /**
     * 转换为VO
     */
    private CustomerVO convertToVO(Customer customer) {
        CustomerVO vo = new CustomerVO();
        vo.setId(customer.getId().toString());
        vo.setName(customer.getName());
        vo.setEmail(customer.getEmail());
        vo.setPhone(customer.getPhone());
        vo.setCompany(customer.getCompany());
        vo.setPosition(customer.getPosition());
        vo.setCountry(customer.getCountry());
        vo.setCountryCode(customer.getCountryCode());
        vo.setAddress(customer.getAddress());
        vo.setWebsite(customer.getWebsite());
        vo.setPriority(customer.getPriority());
        vo.setMeetingTime(customer.getMeetingTime());
        vo.setMeetingLocation(customer.getMeetingLocation());
        vo.setWechatName(customer.getWechatName());
        vo.setWechatQrcode(customer.getWechatQrcode());
        vo.setWhatsappName(customer.getWhatsappName());
        vo.setWhatsappQrcode(customer.getWhatsappQrcode());
        vo.setBusinessCardFront(customer.getBusinessCardFront());
        vo.setBusinessCardBack(customer.getBusinessCardBack());
        vo.setFollowUpStatus(customer.getFollowUpStatus());
        vo.setRemark(customer.getRemark());
        vo.setSource(customer.getSource());
        vo.setSourceFile(customer.getSourceFile());
        vo.setOcrConfidence(customer.getOcrConfidence() != null ? customer.getOcrConfidence().doubleValue() : null);
        vo.setEmailStatus(customer.getEmailStatus());
        vo.setLastEmailTime(customer.getLastEmailTime());
        vo.setEmailCount(customer.getEmailCount());
        vo.setCreatedAt(customer.getCreatedAt());
        vo.setUpdatedAt(customer.getUpdatedAt());
        
        // 转换自定义字段
        if (customer.getExtraData() != null) {
            Map<String, String> customFields = new HashMap<>();
            customer.getExtraData().forEach((k, v) -> customFields.put(k, v != null ? v.toString() : null));
            vo.setCustomFields(customFields);
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadBusinessCard(Long userId, Long customerId, String side, MultipartFile file) {
        // 验证客户归属
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }

        // 验证 side 参数
        if (!"front".equals(side) && !"back".equals(side)) {
            throw new BusinessException("side 参数必须是 front 或 back");
        }

        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }

        // 验证文件类型
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!".jpg".equals(extension) && !".jpeg".equals(extension) && 
            !".png".equals(extension) && !".gif".equals(extension) && 
            !".webp".equals(extension)) {
            throw new BusinessException("只支持 jpg/jpeg/png/gif/webp 格式的图片");
        }

        try {
            // 生成文件路径: uploads/customers/business-cards/{date}/{uuid}.{ext}
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;
            String relativePath = "/uploads/customers/business-cards/" + dateDir + "/" + newFileName;
            
            // 创建目录
            Path targetDir = Paths.get(uploadPath, "customers", "business-cards", dateDir);
            Files.createDirectories(targetDir);
            
            // 保存文件
            Path targetPath = targetDir.resolve(newFileName);
            file.transferTo(targetPath.toFile());
            
            // 删除旧图片（如果存在）
            String oldPath = "front".equals(side) ? customer.getBusinessCardFront() : customer.getBusinessCardBack();
            if (StrUtil.isNotBlank(oldPath)) {
                try {
                    String oldFilePath = oldPath.replace("/uploads/", "");
                    Path oldFile = Paths.get(uploadPath, oldFilePath);
                    Files.deleteIfExists(oldFile);
                } catch (Exception e) {
                    log.warn("删除旧名片图片失败: {}", oldPath, e);
                }
            }
            
            // 更新数据库
            if ("front".equals(side)) {
                customer.setBusinessCardFront(relativePath);
            } else {
                customer.setBusinessCardBack(relativePath);
            }
            customerMapper.updateById(customer);
            
            log.info("客户名片上传成功: customerId={}, side={}, path={}", customerId, side, relativePath);
            return relativePath;
            
        } catch (IOException e) {
            log.error("名片上传失败", e);
            throw new BusinessException("名片上传失败: " + e.getMessage());
        }
    }
}

