package com.trucktools.customer.service;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.customer.dto.CustomerQueryParam;
import com.trucktools.customer.dto.CustomerRequest;
import com.trucktools.customer.dto.CustomerVO;
import com.trucktools.customer.entity.Customer;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 客户服务接口
 */
public interface CustomerService {

    /**
     * 分页查询客户
     */
    PageResult<CustomerVO> getPage(CustomerQueryParam param);

    /**
     * 获取客户详情
     */
    CustomerVO getDetail(Long userId, Long customerId);

    /**
     * 创建客户
     */
    Long create(Long userId, CustomerRequest request);

    /**
     * 更新客户
     */
    void update(Long userId, Long customerId, CustomerRequest request);

    /**
     * 删除客户
     */
    void delete(Long userId, Long customerId);

    /**
     * 批量删除客户
     */
    void batchDelete(Long userId, List<Long> customerIds);

    /**
     * 导出客户
     */
    String export(Long userId, List<Long> ids, Map<String, Object> filter, List<String> fields);

    /**
     * 根据ID获取客户
     */
    Customer getById(Long customerId);

    /**
     * 根据ID列表获取客户
     */
    List<Customer> getByIds(List<Long> customerIds);

    /**
     * 更新客户邮件发送信息
     */
    void updateEmailInfo(Long customerId);

    /**
     * 上传名片图片
     * @param side front 或 back
     */
    String uploadBusinessCard(Long userId, Long customerId, String side, MultipartFile file);
}

