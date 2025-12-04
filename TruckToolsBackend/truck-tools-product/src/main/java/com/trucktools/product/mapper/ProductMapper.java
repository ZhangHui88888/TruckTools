package com.trucktools.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.product.dto.BrandVO;
import com.trucktools.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 产品Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 获取品牌列表及产品数量（按产品数量降序排序）
     */
    @Select("SELECT brand_code as brandCode, brand_name as brandName, COUNT(*) as productCount " +
            "FROM t_product WHERE user_id = #{userId} AND deleted = 0 " +
            "GROUP BY brand_code, brand_name ORDER BY productCount DESC")
    List<BrandVO> selectBrandList(@Param("userId") Long userId);

    /**
     * 根据OE号查询产品（精确匹配）
     */
    @Select("SELECT * FROM t_product WHERE user_id = #{userId} AND oe_no = #{oeNo} AND deleted = 0 LIMIT 1")
    Product selectByOeNo(@Param("userId") Long userId, @Param("oeNo") String oeNo);

    /**
     * 根据OE号模糊查询产品（支持/分隔的多个OE号）
     * 匹配规则：
     * 1. 清除OE号中的连字符后进行匹配
     * 2. 产品的oe_no字段可能包含多个值（用/分隔），只要有一个匹配即可
     */
    @Select("SELECT * FROM t_product WHERE user_id = #{userId} AND deleted = 0 " +
            "AND (REPLACE(oe_no, '-', '') = #{oeNo} " +
            "OR REPLACE(oe_no, '-', '') LIKE CONCAT('%/', #{oeNo}, '/%') " +
            "OR REPLACE(oe_no, '-', '') LIKE CONCAT(#{oeNo}, '/%') " +
            "OR REPLACE(oe_no, '-', '') LIKE CONCAT('%/', #{oeNo})) " +
            "LIMIT 1")
    Product selectByOeNoFuzzy(@Param("userId") Long userId, @Param("oeNo") String oeNo);

    /**
     * 根据多个OE号查询产品
     */
    List<Product> selectByOeNos(@Param("userId") Long userId, @Param("oeNos") List<String> oeNos);
}

