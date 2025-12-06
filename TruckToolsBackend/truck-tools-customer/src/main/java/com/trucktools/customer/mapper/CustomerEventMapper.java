package com.trucktools.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.customer.entity.CustomerEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 客户事件Mapper
 */
@Mapper
public interface CustomerEventMapper extends BaseMapper<CustomerEvent> {

    /**
     * 查询客户的所有事件（按时间倒序）
     */
    @Select("SELECT * FROM t_customer_event WHERE customer_id = #{customerId} AND deleted = 0 ORDER BY event_time DESC, created_at DESC")
    List<CustomerEvent> selectByCustomerId(@Param("customerId") Long customerId);

    /**
     * 查询客户的最新事件
     */
    @Select("SELECT * FROM t_customer_event WHERE customer_id = #{customerId} AND deleted = 0 ORDER BY event_time DESC, created_at DESC LIMIT 1")
    CustomerEvent selectLatestByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 查询用户所有客户的最新事件（每个客户只返回一条）
     */
    @Select("""
        SELECT e.* FROM t_customer_event e
        INNER JOIN (
            SELECT customer_id, MAX(id) as max_id
            FROM t_customer_event
            WHERE user_id = #{userId} AND deleted = 0
            GROUP BY customer_id
        ) latest ON e.id = latest.max_id
        WHERE e.user_id = #{userId} AND e.deleted = 0
        ORDER BY e.event_status ASC, e.event_time DESC
        """)
    List<CustomerEvent> selectLatestEventsPerCustomer(@Param("userId") Long userId);
}

