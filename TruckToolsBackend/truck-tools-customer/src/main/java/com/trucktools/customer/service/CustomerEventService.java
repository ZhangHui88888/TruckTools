package com.trucktools.customer.service;

import com.trucktools.customer.dto.CustomerEventRequest;
import com.trucktools.customer.dto.CustomerEventVO;

import java.util.List;

/**
 * 客户事件服务
 */
public interface CustomerEventService {

    /**
     * 创建事件
     */
    CustomerEventVO create(Long userId, CustomerEventRequest request);

    /**
     * 更新事件
     */
    CustomerEventVO update(Long userId, Long id, CustomerEventRequest request);

    /**
     * 删除事件
     */
    void delete(Long userId, Long id);

    /**
     * 查询客户的所有事件
     */
    List<CustomerEventVO> listByCustomerId(Long userId, Long customerId);

    /**
     * 获取事件详情
     */
    CustomerEventVO getDetail(Long userId, Long id);
}

