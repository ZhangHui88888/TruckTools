package com.trucktools.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.email.entity.EmailTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 邮件模板Mapper
 */
@Mapper
public interface EmailTemplateMapper extends BaseMapper<EmailTemplate> {

    /**
     * 查询用户的模板列表
     */
    @Select("SELECT * FROM t_email_template WHERE user_id = #{userId} AND status = 1 AND deleted = 0 ORDER BY created_at DESC")
    List<EmailTemplate> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户默认模板
     */
    @Select("SELECT * FROM t_email_template WHERE user_id = #{userId} AND is_default = 1 AND status = 1 AND deleted = 0 LIMIT 1")
    EmailTemplate selectDefaultByUserId(@Param("userId") Long userId);
}

