package com.trucktools.email.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.customer.entity.Customer;
import com.trucktools.customer.entity.CustomFieldDefinition;
import com.trucktools.customer.mapper.CustomFieldDefinitionMapper;
import com.trucktools.customer.service.CustomerService;
import com.trucktools.email.dto.EmailTemplateRequest;
import com.trucktools.email.dto.EmailTemplateVO;
import com.trucktools.email.entity.EmailTemplate;
import com.trucktools.email.mapper.EmailTemplateMapper;
import com.trucktools.email.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 邮件模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateMapper templateMapper;
    private final CustomerService customerService;
    private final CustomFieldDefinitionMapper customFieldMapper;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    @Override
    public PageResult<EmailTemplateVO> getPage(Long userId, int page, int pageSize, String category) {
        LambdaQueryWrapper<EmailTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailTemplate::getUserId, userId)
                .eq(EmailTemplate::getStatus, 1);
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(EmailTemplate::getCategory, category);
        }
        
        wrapper.orderByDesc(EmailTemplate::getCreatedAt);
        
        Page<EmailTemplate> pageResult = templateMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        List<EmailTemplateVO> list = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(list, pageResult.getTotal(), page, pageSize);
    }

    @Override
    public EmailTemplateVO getDetail(Long userId, Long templateId) {
        EmailTemplate template = templateMapper.selectById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
        return convertToVO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, EmailTemplateRequest request) {
        EmailTemplate template = new EmailTemplate();
        template.setUserId(userId);
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setContent(request.getContent());
        template.setContentText(stripHtml(request.getContent()));
        template.setCategory(request.getCategory());
        template.setVariables(extractVariables(request.getContent()));
        template.setIsDefault(0);
        template.setUseCount(0);
        template.setStatus(1);
        
        templateMapper.insert(template);
        log.info("创建邮件模板: userId={}, templateId={}", userId, template.getId());
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long templateId, EmailTemplateRequest request) {
        EmailTemplate template = templateMapper.selectById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
        
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setContent(request.getContent());
        template.setContentText(stripHtml(request.getContent()));
        template.setCategory(request.getCategory());
        template.setVariables(extractVariables(request.getContent()));
        
        templateMapper.updateById(template);
        log.info("更新邮件模板: templateId={}", templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long templateId) {
        EmailTemplate template = templateMapper.selectById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
        
        templateMapper.deleteById(templateId);
        log.info("删除邮件模板: templateId={}", templateId);
    }

    @Override
    public Map<String, List<Map<String, String>>> getVariables(Long userId) {
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        
        // 系统变量
        List<Map<String, String>> systemVariables = new ArrayList<>();
        systemVariables.add(Map.of("key", "name", "label", "客户姓名", "example", "John Smith"));
        systemVariables.add(Map.of("key", "email", "label", "邮箱", "example", "john@example.com"));
        systemVariables.add(Map.of("key", "company", "label", "公司名称", "example", "ABC Trading"));
        systemVariables.add(Map.of("key", "position", "label", "职位", "example", "Sales Manager"));
        systemVariables.add(Map.of("key", "country", "label", "国家", "example", "美国"));
        systemVariables.add(Map.of("key", "meetingTime", "label", "会面时间", "example", "2025-11-28"));
        systemVariables.add(Map.of("key", "meetingLocation", "label", "会面地点", "example", "广交会"));
        systemVariables.add(Map.of("key", "wechatName", "label", "微信名称", "example", "john_wechat"));
        systemVariables.add(Map.of("key", "whatsappName", "label", "WhatsApp", "example", "+1234567890"));
        result.put("systemVariables", systemVariables);
        
        // 获取用户自定义变量
        List<CustomFieldDefinition> customFields = customFieldMapper.selectByUserId(userId);
        List<Map<String, String>> customVariables = new ArrayList<>();
        for (CustomFieldDefinition field : customFields) {
            customVariables.add(Map.of(
                    "key", field.getFieldKey(),
                    "label", field.getFieldName(),
                    "example", field.getDefaultValue() != null ? field.getDefaultValue() : ""
            ));
        }
        result.put("customVariables", customVariables);
        
        return result;
    }

    @Override
    public Map<String, Object> preview(Long userId, String subject, String content, Long customerId) {
        Customer customer = customerService.getById(customerId);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }
        
        String renderedSubject = renderContent(subject, customer, null);
        String renderedContent = renderContent(content, customer, null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("subject", renderedSubject);
        result.put("content", renderedContent);
        result.put("customer", Map.of(
                "name", customer.getName(),
                "email", customer.getEmail(),
                "company", customer.getCompany() != null ? customer.getCompany() : ""
        ));
        
        return result;
    }

    @Override
    public EmailTemplate getById(Long templateId) {
        return templateMapper.selectById(templateId);
    }

    @Override
    public String renderContent(String template, Customer customer, Map<String, String> customFields) {
        if (template == null) {
            return "";
        }
        
        String result = template;
        
        // 将纯文本换行符转换为HTML换行标签
        result = result.replace("\r\n", "<br>").replace("\n", "<br>").replace("\r", "<br>");
        
        // 将连续空格转换为HTML不换行空格，保留缩进格式
        // 先将Tab转换为4个空格
        result = result.replace("\t", "    ");
        // 将连续的两个空格替换为 &nbsp; + 空格（交替处理，防止全部替换后浏览器仍合并）
        StringBuilder sb = new StringBuilder();
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ' && i + 1 < chars.length && chars[i + 1] == ' ') {
                sb.append("&nbsp;");
            } else {
                sb.append(chars[i]);
            }
        }
        result = sb.toString();
        
        // 替换系统变量
        result = result.replace("{{name}}", nullToEmpty(customer.getName()));
        result = result.replace("{{email}}", nullToEmpty(customer.getEmail()));
        result = result.replace("{{company}}", nullToEmpty(customer.getCompany()));
        result = result.replace("{{position}}", nullToEmpty(customer.getPosition()));
        result = result.replace("{{country}}", nullToEmpty(customer.getCountry()));
        result = result.replace("{{phone}}", nullToEmpty(customer.getPhone()));
        result = result.replace("{{address}}", nullToEmpty(customer.getAddress()));
        result = result.replace("{{website}}", nullToEmpty(customer.getWebsite()));
        result = result.replace("{{meetingLocation}}", nullToEmpty(customer.getMeetingLocation()));
        result = result.replace("{{wechatName}}", nullToEmpty(customer.getWechatName()));
        result = result.replace("{{whatsappName}}", nullToEmpty(customer.getWhatsappName()));
        
        if (customer.getMeetingTime() != null) {
            result = result.replace("{{meetingTime}}", customer.getMeetingTime().toLocalDate().toString());
        } else {
            result = result.replace("{{meetingTime}}", "");
        }
        
        // 替换自定义变量
        if (customFields != null) {
            for (Map.Entry<String, String> entry : customFields.entrySet()) {
                result = result.replace("{{" + entry.getKey() + "}}", nullToEmpty(entry.getValue()));
            }
        }
        
        // 从extraData中获取自定义字段
        if (customer.getExtraData() != null) {
            for (Map.Entry<String, Object> entry : customer.getExtraData().entrySet()) {
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace("{{" + entry.getKey() + "}}", value);
            }
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUseCount(Long templateId) {
        EmailTemplate template = templateMapper.selectById(templateId);
        if (template != null) {
            template.setUseCount(template.getUseCount() + 1);
            template.setLastUsedAt(LocalDateTime.now());
            templateMapper.updateById(template);
        }
    }

    /**
     * 提取模板中的变量
     */
    private List<String> extractVariables(String content) {
        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return new ArrayList<>(variables);
    }

    /**
     * 去除HTML标签
     */
    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]+>", "").trim();
    }

    /**
     * null转空字符串
     */
    private String nullToEmpty(String str) {
        return str != null ? str : "";
    }

    /**
     * 转换为VO
     */
    private EmailTemplateVO convertToVO(EmailTemplate template) {
        EmailTemplateVO vo = new EmailTemplateVO();
        vo.setId(template.getId().toString());
        vo.setName(template.getName());
        vo.setSubject(template.getSubject());
        vo.setContent(template.getContent());
        vo.setContentText(template.getContentText());
        vo.setVariables(template.getVariables());
        vo.setCategory(template.getCategory());
        vo.setIsDefault(template.getIsDefault() == 1);
        vo.setUseCount(template.getUseCount());
        vo.setLastUsedAt(template.getLastUsedAt());
        vo.setCreatedAt(template.getCreatedAt());
        vo.setUpdatedAt(template.getUpdatedAt());
        return vo;
    }
}

