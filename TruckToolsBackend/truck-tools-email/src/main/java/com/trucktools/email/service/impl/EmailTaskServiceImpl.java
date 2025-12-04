package com.trucktools.email.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.customer.entity.Customer;
import com.trucktools.customer.mapper.CustomerMapper;
import com.trucktools.customer.service.CustomerService;
import com.trucktools.email.dto.CreateTaskRequest;
import com.trucktools.email.dto.EmailLogVO;
import com.trucktools.email.dto.EmailTaskVO;
import com.trucktools.email.entity.EmailAttachment;
import com.trucktools.email.entity.EmailLog;
import com.trucktools.email.entity.EmailTask;
import com.trucktools.email.entity.EmailTemplate;
import com.trucktools.email.mapper.EmailLogMapper;
import com.trucktools.email.mapper.EmailTaskMapper;
import com.trucktools.email.service.EmailAttachmentService;
import com.trucktools.email.service.EmailTaskService;
import com.trucktools.email.service.EmailTemplateService;
import com.trucktools.system.entity.UserSmtpConfig;
import com.trucktools.system.service.SmtpConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 邮件任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTaskServiceImpl implements EmailTaskService {

    private final EmailTaskMapper taskMapper;
    private final EmailLogMapper logMapper;
    private final EmailTemplateService templateService;
    private final EmailAttachmentService attachmentService;
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final SmtpConfigService smtpConfigService;

    @org.springframework.beans.factory.annotation.Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(Long userId, CreateTaskRequest request) {
        // 验证模板
        EmailTemplate template = templateService.getById(request.getTemplateId());
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
        
        // 验证SMTP配置
        UserSmtpConfig smtpConfig = smtpConfigService.getById(request.getSmtpConfigId());
        if (smtpConfig == null || !smtpConfig.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SMTP_CONFIG_ERROR);
        }
        
        // 获取客户列表
        List<Customer> customers;
        if (request.getCustomerIds() != null && !request.getCustomerIds().isEmpty()) {
            customers = customerService.getByIds(request.getCustomerIds());
            customers = customers.stream()
                    .filter(c -> c.getUserId().equals(userId))
                    .collect(Collectors.toList());
        } else {
            // 根据筛选条件获取客户
            LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Customer::getUserId, userId);
            wrapper.eq(Customer::getEmailStatus, 1); // 只发送给有效邮箱
            
            if (request.getFilter() != null) {
                // 应用筛选条件
                Object priority = request.getFilter().get("priority");
                if (priority != null) {
                    if (priority instanceof List) {
                        wrapper.in(Customer::getPriority, (List<?>) priority);
                    } else {
                        wrapper.eq(Customer::getPriority, priority);
                    }
                }
                Object country = request.getFilter().get("country");
                if (country != null) {
                    if (country instanceof List) {
                        wrapper.in(Customer::getCountry, (List<?>) country);
                    } else {
                        wrapper.eq(Customer::getCountry, country);
                    }
                }
            }
            
            customers = customerMapper.selectList(wrapper);
        }
        
        if (customers.isEmpty()) {
            throw new BusinessException("没有符合条件的客户");
        }
        
        // 创建任务
        EmailTask task = new EmailTask();
        task.setUserId(userId);
        task.setTaskName(request.getTaskName());
        task.setTemplateId(request.getTemplateId());
        task.setSmtpConfigId(request.getSmtpConfigId());
        task.setSubject(template.getSubject());
        task.setContent(template.getContent());
        task.setFilterConditions(request.getFilter());
        task.setTotalCount(customers.size());
        task.setSentCount(0);
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setStatus(0); // 待发送
        task.setScheduledAt(request.getScheduledAt());
        
        taskMapper.insert(task);
        
        // 创建发送日志
        for (Customer customer : customers) {
            EmailLog emailLog = new EmailLog();
            emailLog.setTaskId(task.getId());
            emailLog.setUserId(userId);
            emailLog.setCustomerId(customer.getId());
            emailLog.setCustomerName(customer.getName());
            emailLog.setCustomerEmail(customer.getEmail());
            emailLog.setCustomerCompany(customer.getCompany());
            emailLog.setCustomerCountry(customer.getCountry());
            emailLog.setCustomerPriority(customer.getPriority());
            emailLog.setSubject(templateService.renderContent(template.getSubject(), customer, null));
            emailLog.setContent(templateService.renderContent(template.getContent(), customer, null));
            emailLog.setStatus(0); // 待发送
            emailLog.setRetryCount(0);
            
            logMapper.insert(emailLog);
        }
        
        // 更新模板使用次数
        templateService.incrementUseCount(template.getId());
        
        log.info("创建邮件任务: userId={}, taskId={}, customerCount={}", userId, task.getId(), customers.size());
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getId().toString());
        result.put("totalCount", customers.size());
        result.put("status", 0);
        return result;
    }

    @Override
    public PageResult<EmailTaskVO> getPage(Long userId, int page, int pageSize, Integer status) {
        LambdaQueryWrapper<EmailTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailTask::getUserId, userId);
        
        if (status != null) {
            wrapper.eq(EmailTask::getStatus, status);
        }
        
        wrapper.orderByDesc(EmailTask::getCreatedAt);
        
        Page<EmailTask> pageResult = taskMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        List<EmailTaskVO> list = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(list, pageResult.getTotal(), page, pageSize);
    }

    @Override
    public EmailTaskVO getDetail(Long userId, Long taskId) {
        EmailTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void start(Long userId, Long taskId) {
        EmailTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        
        if (task.getStatus() != 0) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "任务状态不正确，无法开始");
        }
        
        task.setStatus(1);
        task.setStartedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        
        // 异步执行发送任务
        executeTask(taskId);
        
        log.info("开始邮件任务: taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pause(Long userId, Long taskId) {
        EmailTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        
        if (task.getStatus() != 1) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "任务未在发送中，无法暂停");
        }
        
        task.setStatus(2);
        taskMapper.updateById(task);
        
        log.info("暂停邮件任务: taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resume(Long userId, Long taskId) {
        EmailTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        
        if (task.getStatus() != 2) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "任务未暂停，无法继续");
        }
        
        task.setStatus(1);
        taskMapper.updateById(task);
        
        // 继续执行
        executeTask(taskId);
        
        log.info("继续邮件任务: taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long taskId) {
        EmailTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        
        if (task.getStatus() == 3 || task.getStatus() == 4) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "任务已完成或已取消");
        }
        
        task.setStatus(4);
        taskMapper.updateById(task);
        
        log.info("取消邮件任务: taskId={}", taskId);
    }

    @Override
    public PageResult<EmailLogVO> getLogs(Long userId, Long taskId, int page, int pageSize, Integer status) {
        // 验证任务权限
        EmailTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        
        LambdaQueryWrapper<EmailLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailLog::getTaskId, taskId);
        
        if (status != null) {
            wrapper.eq(EmailLog::getStatus, status);
        }
        
        wrapper.orderByDesc(EmailLog::getCreatedAt);
        
        Page<EmailLog> pageResult = logMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        List<EmailLogVO> list = pageResult.getRecords().stream()
                .map(this::convertLogToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(list, pageResult.getTotal(), page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(Long userId, Long taskId, List<Long> logIds) {
        EmailTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        
        LambdaQueryWrapper<EmailLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailLog::getTaskId, taskId)
                .eq(EmailLog::getStatus, 3); // 只重试失败的
        
        if (logIds != null && !logIds.isEmpty()) {
            wrapper.in(EmailLog::getId, logIds);
        }
        
        List<EmailLog> logs = logMapper.selectList(wrapper);
        for (EmailLog emailLog : logs) {
            emailLog.setStatus(0); // 重置为待发送
            emailLog.setRetryCount(emailLog.getRetryCount() + 1);
            logMapper.updateById(emailLog);
        }
        
        // 如果任务已完成，重新设置为发送中
        if (task.getStatus() == 3) {
            task.setStatus(1);
            taskMapper.updateById(task);
            executeTask(taskId);
        }
        
        log.info("重试失败邮件: taskId={}, count={}", taskId, logs.size());
    }

    @Override
    public String exportLogs(Long userId, Long taskId) {
        // TODO: 实现日志导出
        return "https://example.com/exports/email_log_" + taskId + ".xlsx";
    }

    @Override
    @Async
    public void executeTask(Long taskId) {
        EmailTask task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 1) {
            return;
        }
        
        UserSmtpConfig smtpConfig = smtpConfigService.getById(task.getSmtpConfigId());
        if (smtpConfig == null) {
            log.error("SMTP配置不存在: taskId={}, smtpConfigId={}", taskId, task.getSmtpConfigId());
            return;
        }
        
        JavaMailSenderImpl mailSender = createMailSender(smtpConfig);
        
        // 获取模板附件
        List<EmailAttachment> attachments = attachmentService.getEntityByTemplateId(task.getTemplateId());
        
        int sentCount = task.getSentCount();
        int successCount = task.getSuccessCount();
        int failedCount = task.getFailedCount();
        
        // 批量获取待发送的日志
        List<EmailLog> pendingLogs = logMapper.selectPendingLogs(taskId, 100);
        
        while (!pendingLogs.isEmpty()) {
            // 检查任务状态
            task = taskMapper.selectById(taskId);
            if (task.getStatus() != 1) {
                break;
            }
            
            for (EmailLog emailLog : pendingLogs) {
                try {
                    // 更新状态为发送中
                    emailLog.setStatus(1);
                    logMapper.updateById(emailLog);
                    
                    // 发送邮件
                    sendEmail(mailSender, smtpConfig, emailLog, attachments);
                    
                    // 更新状态为成功
                    emailLog.setStatus(2);
                    emailLog.setSentAt(LocalDateTime.now());
                    logMapper.updateById(emailLog);
                    
                    successCount++;
                    
                    // 更新客户邮件发送信息
                    customerService.updateEmailInfo(emailLog.getCustomerId());
                    
                } catch (Exception e) {
                    log.error("邮件发送失败: logId={}, error={}", emailLog.getId(), e.getMessage());
                    
                    emailLog.setStatus(3);
                    emailLog.setErrorCode("SEND_FAILED");
                    emailLog.setErrorMessage(e.getMessage());
                    logMapper.updateById(emailLog);
                    
                    failedCount++;
                }
                
                sentCount++;
                
                // 更新任务进度
                if (sentCount % 10 == 0) {
                    taskMapper.updateProgress(taskId, sentCount, successCount, failedCount);
                }
                
                // 控制发送速度
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            // 获取下一批待发送的日志
            pendingLogs = logMapper.selectPendingLogs(taskId, 100);
        }
        
        // 更新最终进度
        taskMapper.updateProgress(taskId, sentCount, successCount, failedCount);
        
        // 检查是否完成
        task = taskMapper.selectById(taskId);
        if (task.getStatus() == 1 && sentCount >= task.getTotalCount()) {
            task.setStatus(3);
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            log.info("邮件任务完成: taskId={}, success={}, failed={}", taskId, successCount, failedCount);
        }
    }

    /**
     * 发送单封邮件
     */
    private void sendEmail(JavaMailSenderImpl mailSender, UserSmtpConfig smtpConfig, 
                           EmailLog emailLog, List<EmailAttachment> attachments) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        // 第二个参数表示是否是 multipart 消息（附件需要）
        boolean hasAttachments = attachments != null && !attachments.isEmpty();
        MimeMessageHelper helper = new MimeMessageHelper(message, hasAttachments, "UTF-8");
        
        helper.setFrom(smtpConfig.getSenderEmail(), smtpConfig.getSenderName());
        helper.setTo(emailLog.getCustomerEmail());
        helper.setSubject(emailLog.getSubject());
        helper.setText(emailLog.getContent(), true);
        
        // 添加附件
        if (hasAttachments) {
            for (EmailAttachment attachment : attachments) {
                File file = new File(uploadPath, attachment.getFilePath());
                if (file.exists()) {
                    helper.addAttachment(attachment.getFileName(), file);
                } else {
                    log.warn("附件文件不存在: {}", attachment.getFilePath());
                }
            }
        }
        
        mailSender.send(message);
    }

    /**
     * 创建JavaMailSender
     */
    private JavaMailSenderImpl createMailSender(UserSmtpConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getSmtpHost());
        mailSender.setPort(config.getSmtpPort());
        mailSender.setUsername(config.getSmtpUsername());
        mailSender.setPassword(config.getSmtpPassword());
        mailSender.setDefaultEncoding("UTF-8");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.connectiontimeout", "10000");

        if (config.getUseSsl() == 1) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        if (config.getUseTls() == 1) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        return mailSender;
    }

    /**
     * 转换为任务VO
     */
    private EmailTaskVO convertToVO(EmailTask task) {
        EmailTaskVO vo = new EmailTaskVO();
        vo.setId(task.getId().toString());
        vo.setTaskName(task.getTaskName());
        vo.setTemplateId(task.getTemplateId().toString());
        vo.setSmtpConfigId(task.getSmtpConfigId().toString());
        vo.setSubject(task.getSubject());
        vo.setContent(task.getContent());
        vo.setTotalCount(task.getTotalCount());
        vo.setSentCount(task.getSentCount());
        vo.setSuccessCount(task.getSuccessCount());
        vo.setFailedCount(task.getFailedCount());
        vo.setStatus(task.getStatus());
        vo.setStatusText(getStatusText(task.getStatus()));
        vo.setProgress(task.getTotalCount() > 0 ? 
                (double) task.getSentCount() / task.getTotalCount() * 100 : 0);
        vo.setScheduledAt(task.getScheduledAt());
        vo.setStartedAt(task.getStartedAt());
        vo.setCompletedAt(task.getCompletedAt());
        vo.setCreatedAt(task.getCreatedAt());
        return vo;
    }

    /**
     * 转换为日志VO
     */
    private EmailLogVO convertLogToVO(EmailLog log) {
        EmailLogVO vo = new EmailLogVO();
        vo.setId(log.getId().toString());
        vo.setTaskId(log.getTaskId().toString());
        vo.setCustomerId(log.getCustomerId().toString());
        vo.setCustomerName(log.getCustomerName());
        vo.setCustomerEmail(log.getCustomerEmail());
        vo.setCustomerCompany(log.getCustomerCompany());
        vo.setCustomerCountry(log.getCustomerCountry());
        vo.setCustomerPriority(log.getCustomerPriority());
        vo.setSubject(log.getSubject());
        vo.setContent(log.getContent());
        vo.setVariablesData(log.getVariablesData());
        vo.setStatus(log.getStatus());
        vo.setStatusText(getLogStatusText(log.getStatus()));
        vo.setErrorCode(log.getErrorCode());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setRetryCount(log.getRetryCount());
        vo.setSentAt(log.getSentAt());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }

    /**
     * 获取任务状态文本
     */
    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待发送";
            case 1 -> "发送中";
            case 2 -> "已暂停";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    /**
     * 获取日志状态文本
     */
    private String getLogStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待发送";
            case 1 -> "发送中";
            case 2 -> "发送成功";
            case 3 -> "发送失败";
            default -> "未知";
        };
    }
}

