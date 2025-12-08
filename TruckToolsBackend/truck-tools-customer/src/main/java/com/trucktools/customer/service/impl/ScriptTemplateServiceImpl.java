package com.trucktools.customer.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.customer.dto.GeneratedScriptVO;
import com.trucktools.customer.dto.ScriptTemplateRequest;
import com.trucktools.customer.dto.ScriptTemplateVO;
import com.trucktools.customer.entity.Customer;
import com.trucktools.customer.entity.ScriptTemplate;
import com.trucktools.customer.mapper.CustomerMapper;
import com.trucktools.customer.mapper.ScriptTemplateMapper;
import com.trucktools.customer.service.ScriptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 话术模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScriptTemplateServiceImpl implements ScriptTemplateService {

    private final ScriptTemplateMapper templateMapper;
    private final CustomerMapper customerMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    /**
     * 支持的变量列表
     */
    private static final List<String> SUPPORTED_VARIABLES = Arrays.asList(
            "name",           // 客户姓名
            "company",        // 公司名称
            "position",       // 职位
            "country",        // 国家
            "email",          // 邮箱
            "phone",          // 手机号
            "wechatName",     // 微信名称
            "whatsappName",   // WhatsApp名称
            "meetingTime",    // 会面时间
            "meetingLocation" // 会面地点
    );

    @Override
    public ScriptTemplateVO create(Long userId, ScriptTemplateRequest request) {
        ScriptTemplate template = new ScriptTemplate();
        template.setId(IdUtil.getSnowflakeNextId());
        template.setUserId(userId);
        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setDescription(request.getDescription());
        template.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        template.setEnabled(request.getEnabled() != null && request.getEnabled() ? 1 : 1);

        templateMapper.insert(template);
        log.info("创建话术模板: userId={}, templateId={}", userId, template.getId());

        return convertToVO(template);
    }

    @Override
    public ScriptTemplateVO update(Long userId, Long templateId, ScriptTemplateRequest request) {
        ScriptTemplate template = templateMapper.selectById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "话术模板不存在");
        }

        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setDescription(request.getDescription());
        if (request.getSortOrder() != null) {
            template.setSortOrder(request.getSortOrder());
        }
        if (request.getEnabled() != null) {
            template.setEnabled(request.getEnabled() ? 1 : 0);
        }

        templateMapper.updateById(template);
        log.info("更新话术模板: userId={}, templateId={}", userId, templateId);

        return convertToVO(template);
    }

    @Override
    public void delete(Long userId, Long templateId) {
        ScriptTemplate template = templateMapper.selectById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "话术模板不存在");
        }

        templateMapper.deleteById(templateId);
        log.info("删除话术模板: userId={}, templateId={}", userId, templateId);
    }

    @Override
    public ScriptTemplateVO getDetail(Long userId, Long templateId) {
        ScriptTemplate template = templateMapper.selectById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "话术模板不存在");
        }
        return convertToVO(template);
    }

    @Override
    public List<ScriptTemplateVO> listByUserId(Long userId) {
        List<ScriptTemplate> templates = templateMapper.selectList(
                new LambdaQueryWrapper<ScriptTemplate>()
                        .eq(ScriptTemplate::getUserId, userId)
                        .eq(ScriptTemplate::getDeleted, 0)
                        .orderByAsc(ScriptTemplate::getSortOrder)
                        .orderByDesc(ScriptTemplate::getCreatedAt)
        );
        return templates.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedScriptVO> generateScriptsForCustomer(Long userId, Long customerId) {
        // 获取客户信息
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }

        // 获取启用的话术模板
        List<ScriptTemplate> templates = templateMapper.selectList(
                new LambdaQueryWrapper<ScriptTemplate>()
                        .eq(ScriptTemplate::getUserId, userId)
                        .eq(ScriptTemplate::getEnabled, 1)
                        .eq(ScriptTemplate::getDeleted, 0)
                        .orderByAsc(ScriptTemplate::getSortOrder)
        );

        List<GeneratedScriptVO> result = new ArrayList<>();
        for (ScriptTemplate template : templates) {
            GeneratedScriptVO vo = new GeneratedScriptVO();
            vo.setTemplateId(template.getId());
            vo.setTemplateName(template.getName());
            vo.setGeneratedContent(replaceVariables(template.getContent(), customer));
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<String> getSupportedVariables() {
        return SUPPORTED_VARIABLES;
    }

    /**
     * 替换模板中的变量
     */
    private String replaceVariables(String content, Customer customer) {
        if (content == null) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);

        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement = getVariableValue(variable, customer);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 获取变量对应的值
     */
    private String getVariableValue(String variable, Customer customer) {
        return switch (variable) {
            case "name" -> nullToEmpty(customer.getName());
            case "company" -> nullToEmpty(customer.getCompany());
            case "position" -> nullToEmpty(customer.getPosition());
            case "country" -> nullToEmpty(customer.getCountry());
            case "email" -> nullToEmpty(customer.getEmail());
            case "phone" -> nullToEmpty(customer.getPhone());
            case "wechatName" -> nullToEmpty(customer.getWechatName());
            case "whatsappName" -> nullToEmpty(customer.getWhatsappName());
            case "meetingTime" -> customer.getMeetingTime() != null 
                    ? customer.getMeetingTime().format(DATE_FORMATTER) 
                    : "";
            case "meetingLocation" -> nullToEmpty(customer.getMeetingLocation());
            default -> "{{" + variable + "}}"; // 未知变量保持原样
        };
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    /**
     * 转换为VO
     */
    private ScriptTemplateVO convertToVO(ScriptTemplate template) {
        ScriptTemplateVO vo = new ScriptTemplateVO();
        vo.setId(template.getId());
        vo.setName(template.getName());
        vo.setContent(template.getContent());
        vo.setDescription(template.getDescription());
        vo.setSortOrder(template.getSortOrder());
        vo.setEnabled(template.getEnabled() != null && template.getEnabled() == 1);
        vo.setCreatedAt(template.getCreatedAt());
        vo.setUpdatedAt(template.getUpdatedAt());
        return vo;
    }
}
