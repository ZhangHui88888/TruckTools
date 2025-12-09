package com.trucktools.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.customer.entity.CustomerEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户事件Mapper
 */
@Mapper
public interface CustomerEventMapper extends BaseMapper<CustomerEvent> {

    /**
     * 查询客户的所有事件（按时间倒序，同一天的按ID倒序）
     */
    @Select("SELECT * FROM t_customer_event WHERE customer_id = #{customerId} AND deleted = 0 ORDER BY event_time DESC, id DESC")
    List<CustomerEvent> selectByCustomerId(@Param("customerId") Long customerId);

    /**
     * 查询客户的最新事件
     */
    @Select("SELECT * FROM t_customer_event WHERE customer_id = #{customerId} AND deleted = 0 ORDER BY event_time DESC, id DESC LIMIT 1")
    CustomerEvent selectLatestByCustomerId(@Param("customerId") Long customerId);

    /**
     * 查询用户所有客户的最新未完结事件（每个客户只返回一条）
     * 只返回未完结(pending_customer, pending_us)的最新事件
     * 如果客户所有事件都已完结，则不返回该客户
     */
    @Select("""
        SELECT e.* FROM t_customer_event e
        INNER JOIN (
            SELECT customer_id, MAX(CONCAT(event_time, '_', LPAD(id, 20, '0'))) as max_key
            FROM t_customer_event
            WHERE user_id = #{userId} AND deleted = 0 
            AND (event_status = 'pending_customer' OR event_status = 'pending_us')
            GROUP BY customer_id
        ) latest ON e.customer_id = latest.customer_id 
            AND CONCAT(e.event_time, '_', LPAD(e.id, 20, '0')) = latest.max_key
        WHERE e.user_id = #{userId} AND e.deleted = 0
        ORDER BY e.event_status ASC, e.event_time DESC
        """)
    List<CustomerEvent> selectLatestEventsPerCustomer(@Param("userId") Long userId);

    /**
     * 查询客户的到期未触发的提醒事件
     */
    @Select("""
        SELECT * FROM t_customer_event 
        WHERE customer_id = #{customerId} 
        AND deleted = 0 
        AND event_type = 'reminder' 
        AND reminder_triggered = 0 
        AND reminder_time <= #{now}
        """)
    List<CustomerEvent> selectDueReminders(@Param("customerId") Long customerId, @Param("now") LocalDateTime now);

    /**
     * 标记提醒事件为已触发
     */
    @Update("UPDATE t_customer_event SET reminder_triggered = 1 WHERE id = #{id}")
    int markReminderTriggered(@Param("id") Long id);

    /**
     * 将客户的所有未完结事件标记为已完结
     */
    @Update("""
        UPDATE t_customer_event 
        SET event_status = 'completed' 
        WHERE customer_id = #{customerId} AND deleted = 0 
        AND event_status IN ('pending_customer', 'pending_us')
        """)
    int completeAllEventsForCustomer(@Param("customerId") Long customerId);

    /**
     * 统计最新事件状态为指定状态的客户数量（排除已停止跟进的客户）
     * 与列表查询逻辑一致：每个客户只看最新的未完结事件
     */
    @Select("""
        SELECT COUNT(*) FROM (
            SELECT e.customer_id, e.event_status
            FROM t_customer_event e
            INNER JOIN (
                SELECT customer_id, MAX(CONCAT(event_time, '_', LPAD(id, 20, '0'))) as max_key
                FROM t_customer_event
                WHERE user_id = #{userId} AND deleted = 0 
                AND (event_status = 'pending_customer' OR event_status = 'pending_us')
                GROUP BY customer_id
            ) latest ON e.customer_id = latest.customer_id 
                AND CONCAT(e.event_time, '_', LPAD(e.id, 20, '0')) = latest.max_key
            INNER JOIN t_customer c ON e.customer_id = c.id
            WHERE e.user_id = #{userId} AND e.deleted = 0 AND e.event_status = #{eventStatus}
            AND (c.stop_follow_up = 0 OR c.stop_follow_up IS NULL) AND c.deleted = 0
        ) t
        """)
    Long countCustomersWithPendingStatus(@Param("userId") Long userId, @Param("eventStatus") String eventStatus);
}
