package com.trucktools.customer.service;

import com.trucktools.customer.dto.CustomFieldRequest;
import com.trucktools.customer.dto.CustomFieldVO;

import java.util.List;

/**
 * 自定义字段服务接口
 */
public interface CustomFieldService {

    /**
     * 获取用户的自定义字段列表
     */
    List<CustomFieldVO> getList(Long userId);

    /**
     * 创建自定义字段
     */
    Long create(Long userId, CustomFieldRequest request);

    /**
     * 更新自定义字段
     */
    void update(Long userId, Long fieldId, CustomFieldRequest request);

    /**
     * 删除自定义字段
     */
    void delete(Long userId, Long fieldId);
}

