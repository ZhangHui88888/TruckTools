package com.trucktools.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.customer.entity.CustomerImport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户导入Mapper
 */
@Mapper
public interface CustomerImportMapper extends BaseMapper<CustomerImport> {
}

