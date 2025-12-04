package com.trucktools.email.service;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.customer.entity.Customer;
import com.trucktools.email.dto.EmailTemplateRequest;
import com.trucktools.email.dto.EmailTemplateVO;
import com.trucktools.email.entity.EmailTemplate;

import java.util.List;
import java.util.Map;

/**
 * 邮件模板服务接口
 */
public interface EmailTemplateService {

    /**
     * 获取模板列表
     */
    PageResult<EmailTemplateVO> getPage(Long userId, int page, int pageSize, String category);

    /**
     * 获取模板详情
     */
    EmailTemplateVO getDetail(Long userId, Long templateId);

    /**
     * 创建模板
     */
    Long create(Long userId, EmailTemplateRequest request);

    /**
     * 更新模板
     */
    void update(Long userId, Long templateId, EmailTemplateRequest request);

    /**
     * 删除模板
     */
    void delete(Long userId, Long templateId);

    /**
     * 获取可用变量列表
     */
    Map<String, List<Map<String, String>>> getVariables(Long userId);

    /**
     * 预览邮件
     */
    Map<String, Object> preview(Long userId, String subject, String content, Long customerId);

    /**
     * 根据ID获取模板
     */
    EmailTemplate getById(Long templateId);

    /**
     * 渲染模板内容
     */
    String renderContent(String template, Customer customer, Map<String, String> customFields);

    /**
     * 更新使用次数
     */
    void incrementUseCount(Long templateId);
}

