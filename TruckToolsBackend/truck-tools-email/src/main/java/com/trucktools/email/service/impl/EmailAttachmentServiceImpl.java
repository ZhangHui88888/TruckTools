package com.trucktools.email.service.impl;

import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.email.dto.EmailAttachmentVO;
import com.trucktools.email.entity.EmailAttachment;
import com.trucktools.email.mapper.EmailAttachmentMapper;
import com.trucktools.email.service.EmailAttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 邮件附件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAttachmentServiceImpl implements EmailAttachmentService {

    private final EmailAttachmentMapper attachmentMapper;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Value("${app.upload.url-prefix:/uploads}")
    private String urlPrefix;

    // 最大附件大小: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 允许的文件类型
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "jpg", "jpeg", "png", "gif", "zip", "rar"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmailAttachmentVO upload(Long userId, Long templateId, MultipartFile file) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过10MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("不支持的文件类型: " + extension);
        }

        // 生成存储路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storageName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = "attachments/" + datePath + "/" + storageName;
        
        Path fullPath = Paths.get(uploadPath, relativePath);

        try {
            // 确保目录存在
            Files.createDirectories(fullPath.getParent());
            // 写入文件
            Files.write(fullPath, file.getBytes());
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败，请稍后重试");
        }

        // 保存附件记录
        EmailAttachment attachment = new EmailAttachment();
        attachment.setUserId(userId);
        attachment.setTemplateId(templateId);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(relativePath);
        attachment.setFileUrl(urlPrefix + "/" + relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        attachment.setFileExtension(extension);
        attachment.setStatus(1);

        attachmentMapper.insert(attachment);

        log.info("上传附件成功: userId={}, templateId={}, fileName={}", userId, templateId, originalFilename);

        return convertToVO(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long attachmentId) {
        EmailAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null || !attachment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 删除文件
        try {
            Path filePath = Paths.get(uploadPath, attachment.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除附件文件失败: {}", e.getMessage());
        }

        // 删除记录
        attachmentMapper.deleteById(attachmentId);

        log.info("删除附件: attachmentId={}", attachmentId);
    }

    @Override
    public List<EmailAttachmentVO> getByTemplateId(Long userId, Long templateId) {
        List<EmailAttachment> attachments = attachmentMapper.selectByTemplateId(templateId);
        return attachments.stream()
                .filter(a -> a.getUserId().equals(userId))
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmailAttachment> getEntityByTemplateId(Long templateId) {
        return attachmentMapper.selectByTemplateId(templateId);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 转换为VO
     */
    private EmailAttachmentVO convertToVO(EmailAttachment attachment) {
        EmailAttachmentVO vo = new EmailAttachmentVO();
        vo.setId(attachment.getId().toString());
        vo.setFileName(attachment.getFileName());
        vo.setFileUrl(attachment.getFileUrl());
        vo.setFileSize(attachment.getFileSize());
        vo.setFileType(attachment.getFileType());
        vo.setFileExtension(attachment.getFileExtension());
        vo.setCreatedAt(attachment.getCreatedAt());
        return vo;
    }
}

