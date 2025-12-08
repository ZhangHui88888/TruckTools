package com.trucktools.customer.service;

import com.trucktools.customer.dto.GeneratedScriptVO;
import com.trucktools.customer.dto.ScriptTemplateRequest;
import com.trucktools.customer.dto.ScriptTemplateVO;

import java.util.List;

/**
 * 话术模板服务接口
 */
public interface ScriptTemplateService {

    /**
     * 创建话术模板
     */
    ScriptTemplateVO create(Long userId, ScriptTemplateRequest request);

    /**
     * 更新话术模板
     */
    ScriptTemplateVO update(Long userId, Long templateId, ScriptTemplateRequest request);

    /**
     * 删除话术模板
     */
    void delete(Long userId, Long templateId);

    /**
     * 获取话术模板详情
     */
    ScriptTemplateVO getDetail(Long userId, Long templateId);

    /**
     * 获取用户的所有话术模板
     */
    List<ScriptTemplateVO> listByUserId(Long userId);

    /**
     * 根据客户信息生成话术列表
     */
    List<GeneratedScriptVO> generateScriptsForCustomer(Long userId, Long customerId);

    /**
     * 获取支持的变量列表
     */
    List<String> getSupportedVariables();
}
