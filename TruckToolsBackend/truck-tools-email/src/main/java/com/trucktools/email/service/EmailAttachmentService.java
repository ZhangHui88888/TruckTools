package com.trucktools.email.service;

import com.trucktools.email.dto.EmailAttachmentVO;
import com.trucktools.email.entity.EmailAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 邮件附件服务接口
 */
public interface EmailAttachmentService {

    /**
     * 上传附件
     */
    EmailAttachmentVO upload(Long userId, Long templateId, MultipartFile file);

    /**
     * 删除附件
     */
    void delete(Long userId, Long attachmentId);

    /**
     * 获取模板的附件列表
     */
    List<EmailAttachmentVO> getByTemplateId(Long userId, Long templateId);

    /**
     * 获取附件实体列表
     */
    List<EmailAttachment> getEntityByTemplateId(Long templateId);
}

