package com.trucktools.product.service;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.product.dto.*;

import java.util.List;

/**
 * 产品服务接口
 */
public interface ProductService {

    /**
     * 分页查询产品列表
     */
    PageResult<ProductVO> getPage(ProductQueryParam param);

    /**
     * 获取产品详情
     */
    ProductVO getDetail(Long userId, Long id);

    /**
     * 创建产品
     */
    Long create(Long userId, ProductRequest request);

    /**
     * 更新产品
     */
    void update(Long userId, Long id, ProductRequest request);

    /**
     * 删除产品
     */
    void delete(Long userId, Long id);

    /**
     * 批量删除产品
     */
    void batchDelete(Long userId, List<Long> ids);

    /**
     * 获取品牌列表
     */
    List<BrandVO> getBrandList(Long userId);

    /**
     * 按OE号搜索产品(支持多个OE号)
     */
    List<ProductVO> searchByOeNo(Long userId, String oeNos);
}

