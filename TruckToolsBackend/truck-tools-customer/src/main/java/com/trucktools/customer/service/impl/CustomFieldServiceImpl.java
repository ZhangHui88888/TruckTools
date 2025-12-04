package com.trucktools.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.customer.dto.CustomFieldRequest;
import com.trucktools.customer.dto.CustomFieldVO;
import com.trucktools.customer.entity.CustomFieldDefinition;
import com.trucktools.customer.mapper.CustomFieldDefinitionMapper;
import com.trucktools.customer.service.CustomFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义字段服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomFieldServiceImpl implements CustomFieldService {

    private final CustomFieldDefinitionMapper fieldMapper;

    @Override
    public List<CustomFieldVO> getList(Long userId) {
        List<CustomFieldDefinition> fields = fieldMapper.selectByUserId(userId);
        return fields.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, CustomFieldRequest request) {
        // 检查字段键名是否已存在
        LambdaQueryWrapper<CustomFieldDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomFieldDefinition::getUserId, userId)
                .eq(CustomFieldDefinition::getFieldKey, request.getFieldKey());
        
        if (fieldMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("字段键名已存在");
        }

        // 获取当前最大排序号
        Integer maxSortOrder = getMaxSortOrder(userId);

        CustomFieldDefinition field = new CustomFieldDefinition();
        field.setUserId(userId);
        field.setFieldKey(request.getFieldKey());
        field.setFieldName(request.getFieldName());
        field.setFieldType(request.getFieldType() != null ? request.getFieldType() : "text");
        field.setFieldOptions(request.getFieldOptions());
        field.setDefaultValue(request.getDefaultValue());
        field.setIsRequired(Boolean.TRUE.equals(request.getIsRequired()) ? 1 : 0);
        field.setSortOrder(maxSortOrder + 1);
        field.setStatus(1);

        fieldMapper.insert(field);
        log.info("创建自定义字段: userId={}, fieldKey={}", userId, request.getFieldKey());
        
        return field.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long fieldId, CustomFieldRequest request) {
        CustomFieldDefinition field = fieldMapper.selectById(fieldId);
        if (field == null || !field.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        field.setFieldName(request.getFieldName());
        field.setFieldType(request.getFieldType() != null ? request.getFieldType() : "text");
        field.setFieldOptions(request.getFieldOptions());
        field.setDefaultValue(request.getDefaultValue());
        field.setIsRequired(Boolean.TRUE.equals(request.getIsRequired()) ? 1 : 0);

        fieldMapper.updateById(field);
        log.info("更新自定义字段: fieldId={}", fieldId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long fieldId) {
        CustomFieldDefinition field = fieldMapper.selectById(fieldId);
        if (field == null || !field.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        fieldMapper.deleteById(fieldId);
        log.info("删除自定义字段: fieldId={}", fieldId);
    }

    /**
     * 获取最大排序号
     */
    private Integer getMaxSortOrder(Long userId) {
        LambdaQueryWrapper<CustomFieldDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomFieldDefinition::getUserId, userId)
                .orderByDesc(CustomFieldDefinition::getSortOrder)
                .last("LIMIT 1");
        
        CustomFieldDefinition field = fieldMapper.selectOne(wrapper);
        return field != null && field.getSortOrder() != null ? field.getSortOrder() : 0;
    }

    /**
     * 转换为VO
     */
    private CustomFieldVO convertToVO(CustomFieldDefinition field) {
        CustomFieldVO vo = new CustomFieldVO();
        vo.setId(field.getId().toString());
        vo.setFieldKey(field.getFieldKey());
        vo.setFieldName(field.getFieldName());
        vo.setFieldType(field.getFieldType());
        vo.setFieldOptions(field.getFieldOptions());
        vo.setIsRequired(field.getIsRequired() == 1);
        vo.setDefaultValue(field.getDefaultValue());
        vo.setSortOrder(field.getSortOrder());
        return vo;
    }
}

