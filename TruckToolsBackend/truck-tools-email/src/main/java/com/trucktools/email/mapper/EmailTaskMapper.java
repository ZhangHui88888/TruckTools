package com.trucktools.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.email.entity.EmailTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 邮件任务Mapper
 */
@Mapper
public interface EmailTaskMapper extends BaseMapper<EmailTask> {

    /**
     * 查询用户的任务列表
     */
    @Select("SELECT * FROM t_email_task WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<EmailTask> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询待发送的任务
     */
    @Select("SELECT * FROM t_email_task WHERE status = 0 AND (scheduled_at IS NULL OR scheduled_at <= NOW()) AND deleted = 0")
    List<EmailTask> selectPendingTasks();

    /**
     * 更新任务进度
     */
    @Update("UPDATE t_email_task SET sent_count = #{sentCount}, success_count = #{successCount}, failed_count = #{failedCount} WHERE id = #{taskId}")
    int updateProgress(@Param("taskId") Long taskId, @Param("sentCount") int sentCount, 
                       @Param("successCount") int successCount, @Param("failedCount") int failedCount);
}

