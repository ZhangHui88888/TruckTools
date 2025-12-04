package com.trucktools.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.customer.entity.BusinessCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 名片Mapper
 */
@Mapper
public interface BusinessCardMapper extends BaseMapper<BusinessCard> {

    /**
     * 根据批次号查询
     */
    @Select("SELECT * FROM t_business_card WHERE upload_batch = #{batchId} AND deleted = 0 ORDER BY created_at")
    List<BusinessCard> selectByBatch(@Param("batchId") String batchId);

    /**
     * 查询待识别的名片
     */
    @Select("SELECT * FROM t_business_card WHERE ocr_status = 0 AND deleted = 0 ORDER BY created_at LIMIT #{limit}")
    List<BusinessCard> selectPendingCards(@Param("limit") int limit);
}

