package com.trucktools.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.customer.entity.CustomFieldDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 自定义字段Mapper
 */
@Mapper
public interface CustomFieldDefinitionMapper extends BaseMapper<CustomFieldDefinition> {

    /**
     * 查询用户的自定义字段
     */
    @Select("SELECT * FROM t_custom_field_definition WHERE user_id = #{userId} AND status = 1 AND deleted = 0 ORDER BY sort_order")
    List<CustomFieldDefinition> selectByUserId(@Param("userId") Long userId);
}

