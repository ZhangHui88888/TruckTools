package com.trucktools.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.system.dto.SmtpConfigRequest;
import com.trucktools.system.dto.SmtpConfigVO;
import com.trucktools.system.entity.UserSmtpConfig;
import com.trucktools.system.mapper.UserSmtpConfigMapper;
import com.trucktools.system.service.SmtpConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SMTP配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpConfigServiceImpl implements SmtpConfigService {

    private final UserSmtpConfigMapper smtpConfigMapper;

    @Override
    public List<SmtpConfigVO> getList(Long userId) {
        List<UserSmtpConfig> configs = smtpConfigMapper.selectByUserId(userId);
        return configs.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public SmtpConfigVO getDetail(Long userId, Long configId) {
        UserSmtpConfig config = smtpConfigMapper.selectById(configId);
        if (config == null || !config.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, SmtpConfigRequest request) {
        UserSmtpConfig config = new UserSmtpConfig();
        config.setUserId(userId);
        copyFromRequest(config, request);
        config.setStatus(1);

        // 如果设为默认，先取消其他默认配置
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultConfig(userId);
        }

        smtpConfigMapper.insert(config);
        log.info("创建SMTP配置: userId={}, configId={}", userId, config.getId());
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long configId, SmtpConfigRequest request) {
        UserSmtpConfig config = smtpConfigMapper.selectById(configId);
        if (config == null || !config.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 如果设为默认，先取消其他默认配置
        if (Boolean.TRUE.equals(request.getIsDefault()) && config.getIsDefault() != 1) {
            clearDefaultConfig(userId);
        }

        copyFromRequest(config, request);
        smtpConfigMapper.updateById(config);
        log.info("更新SMTP配置: configId={}", configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long configId) {
        UserSmtpConfig config = smtpConfigMapper.selectById(configId);
        if (config == null || !config.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        smtpConfigMapper.deleteById(configId);
        log.info("删除SMTP配置: configId={}", configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean testConnection(Long userId, Long configId) {
        UserSmtpConfig config = smtpConfigMapper.selectById(configId);
        if (config == null || !config.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        boolean success = false;
        try {
            JavaMailSenderImpl mailSender = createMailSender(config);
            mailSender.testConnection();
            success = true;
            log.info("SMTP连接测试成功: configId={}", configId);
        } catch (Exception e) {
            log.warn("SMTP连接测试失败: configId={}, error={}", configId, e.getMessage());
        }

        // 更新测试状态
        config.setTestStatus(success ? 1 : 0);
        config.setTestTime(LocalDateTime.now());
        smtpConfigMapper.updateById(config);

        return success;
    }

    @Override
    public UserSmtpConfig getById(Long configId) {
        return smtpConfigMapper.selectById(configId);
    }

    @Override
    public UserSmtpConfig getDefaultConfig(Long userId) {
        return smtpConfigMapper.selectDefaultByUserId(userId);
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
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");

        if (config.getUseSsl() == 1) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        if (config.getUseTls() == 1) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        return mailSender;
    }

    /**
     * 清除默认配置
     */
    private void clearDefaultConfig(Long userId) {
        smtpConfigMapper.update(null, new LambdaUpdateWrapper<UserSmtpConfig>()
                .eq(UserSmtpConfig::getUserId, userId)
                .eq(UserSmtpConfig::getIsDefault, 1)
                .set(UserSmtpConfig::getIsDefault, 0));
    }

    /**
     * 从请求复制属性
     */
    private void copyFromRequest(UserSmtpConfig config, SmtpConfigRequest request) {
        config.setConfigName(request.getConfigName());
        config.setSmtpHost(request.getSmtpHost());
        config.setSmtpPort(request.getSmtpPort());
        config.setSmtpUsername(request.getSmtpUsername());
        config.setSmtpPassword(request.getSmtpPassword());
        config.setSenderName(request.getSenderName());
        config.setSenderEmail(request.getSenderEmail());
        config.setUseSsl(Boolean.TRUE.equals(request.getUseSsl()) ? 1 : 0);
        config.setUseTls(Boolean.TRUE.equals(request.getUseTls()) ? 1 : 0);
        config.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()) ? 1 : 0);
        config.setDailyLimit(request.getDailyLimit());
        config.setHourlyLimit(request.getHourlyLimit());
    }

    /**
     * 转换为VO
     */
    private SmtpConfigVO convertToVO(UserSmtpConfig config) {
        SmtpConfigVO vo = new SmtpConfigVO();
        vo.setId(config.getId().toString());
        vo.setConfigName(config.getConfigName());
        vo.setSmtpHost(config.getSmtpHost());
        vo.setSmtpPort(config.getSmtpPort());
        vo.setSmtpUsername(config.getSmtpUsername());
        vo.setSenderName(config.getSenderName());
        vo.setSenderEmail(config.getSenderEmail());
        vo.setUseSsl(config.getUseSsl() == 1);
        vo.setUseTls(config.getUseTls() == 1);
        vo.setIsDefault(config.getIsDefault() == 1);
        vo.setStatus(config.getStatus());
        vo.setTestStatus(config.getTestStatus());
        vo.setTestTime(config.getTestTime());
        vo.setDailyLimit(config.getDailyLimit());
        vo.setHourlyLimit(config.getHourlyLimit());
        vo.setCreatedAt(config.getCreatedAt());
        return vo;
    }
}

