package com.trucktools.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.email.entity.EmailLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 邮件日志Mapper
 */
@Mapper
public interface EmailLogMapper extends BaseMapper<EmailLog> {

    /**
     * 根据任务ID查询日志
     */
    @Select("SELECT * FROM t_email_log WHERE task_id = #{taskId} ORDER BY created_at DESC")
    List<EmailLog> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询待发送的日志
     */
    @Select("SELECT * FROM t_email_log WHERE task_id = #{taskId} AND status = 0 ORDER BY created_at LIMIT #{limit}")
    List<EmailLog> selectPendingLogs(@Param("taskId") Long taskId, @Param("limit") int limit);

    /**
     * 查询失败的日志
     */
    @Select("SELECT * FROM t_email_log WHERE task_id = #{taskId} AND status = 3 ORDER BY created_at")
    List<EmailLog> selectFailedLogs(@Param("taskId") Long taskId);

    /**
     * 统计任务日志
     */
    @Select("SELECT status, COUNT(*) as count FROM t_email_log WHERE task_id = #{taskId} GROUP BY status")
    List<java.util.Map<String, Object>> countByTaskId(@Param("taskId") Long taskId);
}

