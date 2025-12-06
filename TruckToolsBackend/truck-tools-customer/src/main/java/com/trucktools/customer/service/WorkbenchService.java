package com.trucktools.customer.service;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.customer.dto.*;

/**
 * 工作台服务接口
 */
public interface WorkbenchService {

    /**
     * 获取工作台统计数据
     */
    WorkbenchStatsVO getStats(Long userId);

    /**
     * 获取工作台事件列表
     */
    PageResult<WorkbenchEventVO> getEventList(Long userId, WorkbenchEventQueryRequest request);

    /**
     * 处理事件（完成处理后自动新增等待客户反馈事件）
     */
    WorkbenchEventVO processEvent(Long userId, ProcessEventRequest request);

    /**
     * 停止跟进客户
     */
    void stopFollowUp(Long userId, StopFollowUpRequest request);

    /**
     * 恢复跟进客户
     */
    void resumeFollowUp(Long userId, Long customerId);

    /**
     * 检查并创建超时提醒事件（定时任务调用）
     */
    int checkAndCreateOverdueReminders();
}
