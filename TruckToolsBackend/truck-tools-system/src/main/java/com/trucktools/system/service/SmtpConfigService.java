package com.trucktools.system.service;

import com.trucktools.system.dto.SmtpConfigRequest;
import com.trucktools.system.dto.SmtpConfigVO;
import com.trucktools.system.entity.UserSmtpConfig;

import java.util.List;

/**
 * SMTP配置服务接口
 */
public interface SmtpConfigService {

    /**
     * 获取用户SMTP配置列表
     */
    List<SmtpConfigVO> getList(Long userId);

    /**
     * 获取配置详情
     */
    SmtpConfigVO getDetail(Long userId, Long configId);

    /**
     * 创建SMTP配置
     */
    Long create(Long userId, SmtpConfigRequest request);

    /**
     * 更新SMTP配置
     */
    void update(Long userId, Long configId, SmtpConfigRequest request);

    /**
     * 删除SMTP配置
     */
    void delete(Long userId, Long configId);

    /**
     * 测试SMTP连接
     */
    boolean testConnection(Long userId, Long configId);

    /**
     * 根据ID获取配置实体
     */
    UserSmtpConfig getById(Long configId);

    /**
     * 获取用户默认配置
     */
    UserSmtpConfig getDefaultConfig(Long userId);
}

