package com.trucktools.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.product.entity.ProductImport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品导入记录Mapper
 */
@Mapper
public interface ProductImportMapper extends BaseMapper<ProductImport> {
}

