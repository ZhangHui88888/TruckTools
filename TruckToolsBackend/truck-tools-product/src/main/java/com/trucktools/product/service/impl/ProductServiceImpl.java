package com.trucktools.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.product.dto.*;
import com.trucktools.product.entity.Product;
import com.trucktools.product.mapper.ProductMapper;
import com.trucktools.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String uploadBaseUrl;

    @Override
    public PageResult<ProductVO> getPage(ProductQueryParam param) {
        Page<Product> page = new Page<>(param.getPage(), param.getPageSize());
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, param.getUserId())
                .like(StrUtil.isNotBlank(param.getKeyword()), Product::getOeNo, param.getKeyword())
                .or(StrUtil.isNotBlank(param.getKeyword()))
                .like(StrUtil.isNotBlank(param.getKeyword()), Product::getXkNo, param.getKeyword())
                .eq(StrUtil.isNotBlank(param.getBrandCode()), Product::getBrandCode, param.getBrandCode())
                .eq(StrUtil.isNotBlank(param.getOeNo()), Product::getOeNo, param.getOeNo());
        
        // 排序（默认按XK NO.升序）
        if (StrUtil.isNotBlank(param.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(param.getSortOrder());
            switch (param.getSortField()) {
                case "brandCode" -> wrapper.orderBy(true, isAsc, Product::getBrandCode);
                case "xkNo" -> wrapper.orderBy(true, isAsc, Product::getXkNo);
                case "oeNo" -> wrapper.orderBy(true, isAsc, Product::getOeNo);
                case "priceAvg" -> wrapper.orderBy(true, isAsc, Product::getPriceAvg);
                case "createdAt" -> wrapper.orderBy(true, isAsc, Product::getCreatedAt);
                default -> wrapper.orderByAsc(Product::getXkNo);
            }
        } else {
            wrapper.orderByAsc(Product::getXkNo);
        }

        Page<Product> result = productMapper.selectPage(page, wrapper);
        
        List<ProductVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), param.getPage(), param.getPageSize());
    }

    @Override
    public ProductVO getDetail(Long userId, Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("产品不存在");
        }
        return toVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, ProductRequest request) {
        Product product = new Product();
        BeanUtil.copyProperties(request, product);
        product.setUserId(userId);
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("产品不存在");
        }
        BeanUtil.copyProperties(request, product, "id", "userId", "createdAt");
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("产品不存在");
        }
        productMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId)
                .in(Product::getId, ids);
        productMapper.delete(wrapper);
    }

    @Override
    public List<BrandVO> getBrandList(Long userId) {
        return productMapper.selectBrandList(userId);
    }

    @Override
    public List<ProductVO> searchByOeNo(Long userId, String oeNos) {
        if (StrUtil.isBlank(oeNos)) {
            return new ArrayList<>();
        }
        
        // 支持逗号、换行分隔的多个OE号
        List<String> oeNoList = Arrays.stream(oeNos.split("[,;\n\r]+"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        if (oeNoList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Product> products = productMapper.selectByOeNos(userId, oeNoList);
        return products.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private ProductVO toVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        vo.setId(String.valueOf(product.getId()));
        
        // 生成完整图片URL
        if (StrUtil.isNotBlank(product.getImagePath())) {
            vo.setImageUrl(uploadBaseUrl + product.getImagePath());
        }
        
        return vo;
    }
}

