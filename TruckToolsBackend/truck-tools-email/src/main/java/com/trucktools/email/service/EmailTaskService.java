package com.trucktools.email.service;

import com.trucktools.common.core.domain.PageResult;
import com.trucktools.email.dto.CreateTaskRequest;
import com.trucktools.email.dto.EmailLogVO;
import com.trucktools.email.dto.EmailTaskVO;

import java.util.List;
import java.util.Map;

/**
 * 邮件任务服务接口
 */
public interface EmailTaskService {

    /**
     * 创建发送任务
     */
    Map<String, Object> create(Long userId, CreateTaskRequest request);

    /**
     * 获取任务列表
     */
    PageResult<EmailTaskVO> getPage(Long userId, int page, int pageSize, Integer status);

    /**
     * 获取任务详情
     */
    EmailTaskVO getDetail(Long userId, Long taskId);

    /**
     * 开始发送
     */
    void start(Long userId, Long taskId);

    /**
     * 暂停发送
     */
    void pause(Long userId, Long taskId);

    /**
     * 继续发送
     */
    void resume(Long userId, Long taskId);

    /**
     * 取消任务
     */
    void cancel(Long userId, Long taskId);

    /**
     * 获取发送日志
     */
    PageResult<EmailLogVO> getLogs(Long userId, Long taskId, int page, int pageSize, Integer status);

    /**
     * 重试失败的邮件
     */
    void retry(Long userId, Long taskId, List<Long> logIds);

    /**
     * 导出发送日志
     */
    String exportLogs(Long userId, Long taskId);

    /**
     * 执行发送任务
     */
    void executeTask(Long taskId);
}

