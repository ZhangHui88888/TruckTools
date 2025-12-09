package com.trucktools.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.common.utils.IdGenerator;
import com.trucktools.customer.dto.*;
import com.trucktools.customer.entity.Customer;
import com.trucktools.customer.entity.CustomerEvent;
import com.trucktools.customer.mapper.CustomerEventMapper;
import com.trucktools.customer.mapper.CustomerMapper;
import com.trucktools.customer.service.WorkbenchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作台服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkbenchServiceImpl implements WorkbenchService {

    private final CustomerMapper customerMapper;
    private final CustomerEventMapper customerEventMapper;
    private final ObjectMapper objectMapper;

    private static final int OVERDUE_DAYS = 3;
    private static final String EVENT_STATUS_PENDING_US = "pending_us";
    private static final String EVENT_STATUS_PENDING_CUSTOMER = "pending_customer";

    @Override
    public WorkbenchStatsVO getStats(Long userId) {
        WorkbenchStatsVO stats = new WorkbenchStatsVO();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime overdueThreshold = now.minusDays(OVERDUE_DAYS);

        // 待处理客户数量（有等待我方处理事件的客户数，每个客户只计一次）
        Long pendingCount = customerEventMapper.countCustomersWithPendingStatus(userId, EVENT_STATUS_PENDING_US);
        stats.setPendingCount(pendingCount.intValue());

        // 等待反馈客户数量（有等待客户反馈事件的客户数，每个客户只计一次）
        Long waitingCount = customerEventMapper.countCustomersWithPendingStatus(userId, EVENT_STATUS_PENDING_CUSTOMER);
        stats.setWaitingCount(waitingCount.intValue());

        // 今日处理数量（今天状态变为pending_customer的事件）
        Long todayProcessedCount = customerEventMapper.selectCount(
                new LambdaQueryWrapper<CustomerEvent>()
                        .eq(CustomerEvent::getUserId, userId)
                        .eq(CustomerEvent::getEventStatus, EVENT_STATUS_PENDING_CUSTOMER)
                        .ge(CustomerEvent::getCreatedAt, todayStart)
                        .eq(CustomerEvent::getDeleted, 0)
        );
        stats.setTodayProcessedCount(todayProcessedCount.intValue());

        // 超时未跟进客户数量（等待客户反馈超过3天且未停止跟进）
        Long overdueCount = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .eq(Customer::getFollowUpStatus, EVENT_STATUS_PENDING_CUSTOMER)
                        .le(Customer::getLastEventTime, overdueThreshold)
                        .and(w -> w.eq(Customer::getStopFollowUp, 0).or().isNull(Customer::getStopFollowUp))
                        .eq(Customer::getDeleted, 0)
        );
        stats.setOverdueCount(overdueCount.intValue());

        // 本周新增客户数量
        Long weeklyNewCount = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .ge(Customer::getCreatedAt, weekStart)
                        .eq(Customer::getDeleted, 0)
        );
        stats.setWeeklyNewCustomerCount(weeklyNewCount.intValue());

        // 活跃客户数量（近7天有事件更新）
        Long activeCount = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .ge(Customer::getLastEventTime, weekStart)
                        .eq(Customer::getDeleted, 0)
        );
        stats.setActiveCustomerCount(activeCount.intValue());

        // 总客户数量
        Long totalCount = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .eq(Customer::getDeleted, 0)
        );
        stats.setTotalCustomerCount(totalCount.intValue());

        // 已停止跟进客户数量
        Long stoppedCount = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .eq(Customer::getStopFollowUp, 1)
                        .eq(Customer::getDeleted, 0)
        );
        stats.setStoppedFollowUpCount(stoppedCount.intValue());

        return stats;
    }

    @Override
    public PageResult<WorkbenchEventVO> getEventList(Long userId, WorkbenchEventQueryRequest request) {
        // 获取每个客户的最新事件
        List<CustomerEvent> allLatestEvents = customerEventMapper.selectLatestEventsPerCustomer(userId);
        
        // 转换为VO并应用筛选
        List<WorkbenchEventVO> voList = allLatestEvents.stream()
                .map(this::convertToWorkbenchEventVO)
                .collect(Collectors.toList());

        // 事件状态筛选
        if (StringUtils.hasText(request.getEventStatus()) && !"all".equals(request.getEventStatus())) {
            voList = voList.stream()
                    .filter(vo -> request.getEventStatus().equals(vo.getEventStatus()))
                    .collect(Collectors.toList());
        }

        // 时间范围筛选
        if (request.getStartTime() != null) {
            voList = voList.stream()
                    .filter(vo -> vo.getEventTime() != null && !vo.getEventTime().isBefore(request.getStartTime()))
                    .collect(Collectors.toList());
        }
        if (request.getEndTime() != null) {
            voList = voList.stream()
                    .filter(vo -> vo.getEventTime() != null && !vo.getEventTime().isAfter(request.getEndTime()))
                    .collect(Collectors.toList());
        }

        // 客户关键词筛选
        if (StringUtils.hasText(request.getCustomerKeyword())) {
            voList = voList.stream()
                    .filter(vo -> vo.getCustomerName() != null && 
                            vo.getCustomerName().contains(request.getCustomerKeyword()))
                    .collect(Collectors.toList());
        }

        // 排除已停止跟进的客户
        if (Boolean.TRUE.equals(request.getExcludeStoppedFollowUp())) {
            List<Long> stoppedCustomerIds = getStoppedFollowUpCustomerIds(userId);
            voList = voList.stream()
                    .filter(vo -> !stoppedCustomerIds.contains(vo.getCustomerId()))
                    .collect(Collectors.toList());
        }

        // 只显示超时事件
        if (Boolean.TRUE.equals(request.getOverdueOnly())) {
            voList = voList.stream()
                    .filter(WorkbenchEventVO::getIsOverdue)
                    .collect(Collectors.toList());
        }

        // 手动分页
        long total = voList.size();
        int start = (request.getPage() - 1) * request.getPageSize();
        int end = Math.min(start + request.getPageSize(), voList.size());
        
        if (start >= voList.size()) {
            voList = new ArrayList<>();
        } else {
            voList = voList.subList(start, end);
        }

        return PageResult.of(voList, total, request.getPage(), request.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkbenchEventVO processEvent(Long userId, ProcessEventRequest request) {
        // 查询原事件
        CustomerEvent originalEvent = customerEventMapper.selectById(request.getEventIdAsLong());
        if (originalEvent == null || originalEvent.getDeleted() == 1) {
            throw new BusinessException("事件不存在");
        }
        if (!originalEvent.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此事件");
        }

        // 创建新的事件（处理记录）
        CustomerEvent newEvent = new CustomerEvent();
        newEvent.setId(IdGenerator.nextId());
        newEvent.setUserId(userId);
        newEvent.setCustomerId(originalEvent.getCustomerId());
        LocalDateTime processTime = request.getProcessTimeAsLocalDateTime();
        newEvent.setEventTime(processTime != null ? processTime : LocalDateTime.now());
        newEvent.setEventContent(request.getProcessContent());
        newEvent.setEventStatus(EVENT_STATUS_PENDING_CUSTOMER); // 处理完成后等待客户反馈
        newEvent.setIsSystemGenerated(0);
        newEvent.setParentEventId(originalEvent.getId());
        
        // 处理附件
        if (request.getAttachmentUrls() != null && !request.getAttachmentUrls().isEmpty()) {
            try {
                newEvent.setAttachmentUrls(objectMapper.writeValueAsString(request.getAttachmentUrls()));
            } catch (Exception e) {
                log.error("序列化附件URL失败", e);
            }
        }

        customerEventMapper.insert(newEvent);

        // 更新客户的跟进状态和最后事件时间
        Customer customer = customerMapper.selectById(originalEvent.getCustomerId());
        if (customer != null) {
            customer.setFollowUpStatus(EVENT_STATUS_PENDING_CUSTOMER);
            customer.setLastEventTime(newEvent.getEventTime());
            // 减少待处理事件计数
            customer.setPendingEventCount(Math.max(0, 
                    (customer.getPendingEventCount() != null ? customer.getPendingEventCount() : 0) - 1));
            customerMapper.updateById(customer);
        }

        return convertToWorkbenchEventVO(newEvent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopFollowUp(Long userId, StopFollowUpRequest request) {
        Customer customer = customerMapper.selectById(request.getCustomerId());
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在");
        }
        if (!customer.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此客户");
        }

        customer.setStopFollowUp(1);
        customer.setStopFollowUpTime(LocalDateTime.now());
        customer.setStopFollowUpReason(request.getReason());
        customerMapper.updateById(customer);

        log.info("用户{}停止跟进客户{}, 原因: {}", userId, request.getCustomerId(), request.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeFollowUp(Long userId, Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在");
        }
        if (!customer.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此客户");
        }

        customer.setStopFollowUp(0);
        customer.setStopFollowUpTime(null);
        customer.setStopFollowUpReason(null);
        customerMapper.updateById(customer);

        log.info("用户{}恢复跟进客户{}", userId, customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndCreateOverdueReminders() {
        LocalDateTime overdueThreshold = LocalDateTime.now().minusDays(OVERDUE_DAYS);
        int createdCount = 0;

        // 查询所有超时且未停止跟进的客户
        List<Customer> overdueCustomers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getFollowUpStatus, EVENT_STATUS_PENDING_CUSTOMER)
                        .le(Customer::getLastEventTime, overdueThreshold)
                        .and(w -> w.eq(Customer::getStopFollowUp, 0).or().isNull(Customer::getStopFollowUp))
                        .eq(Customer::getDeleted, 0)
        );

        for (Customer customer : overdueCustomers) {
            // 检查是否已经创建过超时提醒（避免重复创建）
            Long existingReminder = customerEventMapper.selectCount(
                    new LambdaQueryWrapper<CustomerEvent>()
                            .eq(CustomerEvent::getCustomerId, customer.getId())
                            .eq(CustomerEvent::getIsSystemGenerated, 1)
                            .eq(CustomerEvent::getEventStatus, EVENT_STATUS_PENDING_US)
                            .ge(CustomerEvent::getCreatedAt, overdueThreshold)
                            .eq(CustomerEvent::getDeleted, 0)
            );

            if (existingReminder > 0) {
                continue; // 已有未处理的超时提醒，跳过
            }

            // 获取最后一次事件
            CustomerEvent lastEvent = customerEventMapper.selectLatestByCustomerId(customer.getId());
            long daysSinceLastEvent = customer.getLastEventTime() != null ? 
                    ChronoUnit.DAYS.between(customer.getLastEventTime(), LocalDateTime.now()) : OVERDUE_DAYS;
            
            // 确保天数至少是OVERDUE_DAYS（因为查询条件已经过滤了超时的客户）
            if (daysSinceLastEvent < OVERDUE_DAYS) {
                continue; // 实际未超时，跳过
            }

            // 先将该客户的所有未完结事件标记为已完结
            customerEventMapper.completeAllEventsForCustomer(customer.getId());

            // 创建超时提醒事件
            CustomerEvent reminderEvent = new CustomerEvent();
            reminderEvent.setId(IdGenerator.nextId());
            reminderEvent.setUserId(customer.getUserId());
            reminderEvent.setCustomerId(customer.getId());
            reminderEvent.setEventTime(LocalDateTime.now());
            reminderEvent.setEventContent(String.format(
                    "【系统提醒】客户 %s 已超过 %d 天未反馈\n上次沟通时间：%s\n上次沟通内容：%s\n建议：请及时跟进，了解客户最新情况",
                    customer.getName(),
                    daysSinceLastEvent,
                    lastEvent != null ? lastEvent.getEventTime().toString() : "无",
                    lastEvent != null ? truncateContent(lastEvent.getEventContent(), 100) : "无"
            ));
            reminderEvent.setEventStatus(EVENT_STATUS_PENDING_US);
            reminderEvent.setIsSystemGenerated(1);
            reminderEvent.setParentEventId(lastEvent != null ? lastEvent.getId() : null);

            customerEventMapper.insert(reminderEvent);

            // 更新客户状态
            customer.setFollowUpStatus(EVENT_STATUS_PENDING_US);
            customer.setLastEventTime(LocalDateTime.now());
            customer.setPendingEventCount((customer.getPendingEventCount() != null ? 
                    customer.getPendingEventCount() : 0) + 1);
            customerMapper.updateById(customer);

            createdCount++;
            log.info("为客户{}创建超时提醒事件", customer.getId());
        }

        log.info("超时提醒检查完成，共创建{}条提醒", createdCount);
        return createdCount;
    }

    /**
     * 转换为工作台事件VO
     */
    private WorkbenchEventVO convertToWorkbenchEventVO(CustomerEvent event) {
        WorkbenchEventVO vo = new WorkbenchEventVO();
        vo.setId(event.getId());
        vo.setCustomerId(event.getCustomerId());
        vo.setEventTime(event.getEventTime());
        vo.setEventLocation(event.getEventLocation());
        vo.setEventContent(event.getEventContent());
        vo.setEventStatus(event.getEventStatus());
        vo.setIsSystemGenerated(event.getIsSystemGenerated() != null && event.getIsSystemGenerated() == 1);
        vo.setCreatedAt(event.getCreatedAt());
        vo.setUpdatedAt(event.getUpdatedAt());

        // 计算等待天数
        long waitingDays = ChronoUnit.DAYS.between(event.getEventTime(), LocalDateTime.now());
        vo.setWaitingDays((int) waitingDays);
        vo.setIsOverdue(waitingDays >= OVERDUE_DAYS && EVENT_STATUS_PENDING_CUSTOMER.equals(event.getEventStatus()));

        // 解析附件URL
        if (StringUtils.hasText(event.getAttachmentUrls())) {
            try {
                List<String> urls = objectMapper.readValue(event.getAttachmentUrls(), 
                        new TypeReference<List<String>>() {});
                vo.setAttachmentUrls(urls);
            } catch (Exception e) {
                log.error("解析附件URL失败", e);
                vo.setAttachmentUrls(new ArrayList<>());
            }
        }

        // 获取客户信息
        Customer customer = customerMapper.selectById(event.getCustomerId());
        if (customer != null) {
            vo.setCustomerName(customer.getName());
            vo.setCustomerCompany(customer.getCompany());
            vo.setCustomerPriority(customer.getPriority());
        }

        return vo;
    }

    /**
     * 获取已停止跟进的客户ID列表
     */
    private List<Long> getStoppedFollowUpCustomerIds(Long userId) {
        List<Customer> stoppedCustomers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .eq(Customer::getStopFollowUp, 1)
                        .eq(Customer::getDeleted, 0)
                        .select(Customer::getId)
        );
        return stoppedCustomers.stream()
                .map(Customer::getId)
                .collect(Collectors.toList());
    }

    /**
     * 截断内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
