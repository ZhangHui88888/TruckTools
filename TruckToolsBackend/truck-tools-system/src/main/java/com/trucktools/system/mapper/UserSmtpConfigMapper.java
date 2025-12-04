package com.trucktools.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.system.entity.UserSmtpConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户SMTP配置Mapper
 */
@Mapper
public interface UserSmtpConfigMapper extends BaseMapper<UserSmtpConfig> {

    /**
     * 查询用户的SMTP配置列表
     */
    @Select("SELECT * FROM t_user_smtp_config WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, created_at DESC")
    List<UserSmtpConfig> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户默认SMTP配置
     */
    @Select("SELECT * FROM t_user_smtp_config WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0 LIMIT 1")
    UserSmtpConfig selectDefaultByUserId(@Param("userId") Long userId);
}

