package com.trucktools.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trucktools.customer.dto.CustomerQueryParam;
import com.trucktools.customer.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户Mapper
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM t_customer WHERE user_id = #{userId} AND email = #{email} AND deleted = 0")
    int existsByEmail(@Param("userId") Long userId, @Param("email") String email);

    /**
     * 分页查询客户（带条件）
     */
    IPage<Customer> selectPageWithCondition(Page<Customer> page, @Param("param") CustomerQueryParam param);
}

