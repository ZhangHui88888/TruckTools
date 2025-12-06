package com.trucktools.customer.service.impl;

import cn.hutool.core.util.IdUtil;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.customer.dto.CustomerEventRequest;
import com.trucktools.customer.dto.CustomerEventVO;
import com.trucktools.customer.entity.Customer;
import com.trucktools.customer.entity.CustomerEvent;
import com.trucktools.customer.mapper.CustomerEventMapper;
import com.trucktools.customer.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户事件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerEventServiceImpl implements com.trucktools.customer.service.CustomerEventService {

    private final CustomerEventMapper eventMapper;
    private final CustomerMapper customerMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerEventVO create(Long userId, CustomerEventRequest request) {

        // 检查客户是否存在
        Customer customer = customerMapper.selectById(request.getCustomerId());
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }

        // 创建事件
        CustomerEvent event = new CustomerEvent();
        event.setId(IdUtil.getSnowflakeNextId());
        event.setUserId(userId);
        event.setCustomerId(request.getCustomerId());
        
        // 解析时间
        try {
            LocalDate eventDate = LocalDate.parse(request.getEventTime(), FORMATTER);
            event.setEventTime(eventDate.atStartOfDay());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "事件时间格式错误，应为：yyyy-MM-dd");
        }
        
        event.setEventLocation(request.getEventLocation());
        event.setEventContent(request.getEventContent());
        event.setEventStatus(request.getEventStatus());

        eventMapper.insert(event);

        // 更新客户的跟进状态为最新事件的状态
        updateCustomerFollowUpStatus(request.getCustomerId());

        return convertToVO(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerEventVO update(Long userId, Long id, CustomerEventRequest request) {

        // 检查事件是否存在
        CustomerEvent event = eventMapper.selectById(id);
        if (event == null || !event.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "事件不存在");
        }

        // 检查客户是否存在
        Customer customer = customerMapper.selectById(request.getCustomerId());
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }

        // 更新事件
        event.setCustomerId(request.getCustomerId());
        
        // 解析时间
        try {
            LocalDate eventDate = LocalDate.parse(request.getEventTime(), FORMATTER);
            event.setEventTime(eventDate.atStartOfDay());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "事件时间格式错误，应为：yyyy-MM-dd");
        }
        
        event.setEventLocation(request.getEventLocation());
        event.setEventContent(request.getEventContent());
        event.setEventStatus(request.getEventStatus());

        eventMapper.updateById(event);

        // 更新客户的跟进状态为最新事件的状态
        updateCustomerFollowUpStatus(request.getCustomerId());

        return convertToVO(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {

        // 检查事件是否存在
        CustomerEvent event = eventMapper.selectById(id);
        if (event == null || !event.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "事件不存在");
        }

        Long customerId = event.getCustomerId();
        
        // 删除事件
        eventMapper.deleteById(id);

        // 更新客户的跟进状态为最新事件的状态
        updateCustomerFollowUpStatus(customerId);
    }

    @Override
    public List<CustomerEventVO> listByCustomerId(Long userId, Long customerId) {

        // 检查客户是否存在
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }

        List<CustomerEvent> events = eventMapper.selectByCustomerId(customerId);
        return events.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerEventVO getDetail(Long userId, Long id) {

        CustomerEvent event = eventMapper.selectById(id);
        if (event == null || !event.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "事件不存在");
        }

        return convertToVO(event);
    }

    /**
     * 更新客户的跟进状态为最新事件的状态
     */
    private void updateCustomerFollowUpStatus(Long customerId) {
        // 获取该客户的最新事件
        CustomerEvent latestEvent = eventMapper.selectLatestByCustomerId(customerId);
        
        // 更新客户的跟进状态
        Customer customer = customerMapper.selectById(customerId);
        if (customer != null) {
            if (latestEvent != null) {
                customer.setFollowUpStatus(latestEvent.getEventStatus());
            } else {
                // 如果没有事件了，设置为默认状态
                customer.setFollowUpStatus("pending_us");
            }
            customerMapper.updateById(customer);
        }
    }

    /**
     * 转换为VO
     */
    private CustomerEventVO convertToVO(CustomerEvent event) {
        CustomerEventVO vo = new CustomerEventVO();
        vo.setId(String.valueOf(event.getId()));
        vo.setCustomerId(String.valueOf(event.getCustomerId()));
        vo.setEventTime(event.getEventTime());
        vo.setEventLocation(event.getEventLocation());
        vo.setEventContent(event.getEventContent());
        vo.setEventStatus(event.getEventStatus());
        vo.setCreatedAt(event.getCreatedAt());
        vo.setUpdatedAt(event.getUpdatedAt());
        return vo;
    }
}

