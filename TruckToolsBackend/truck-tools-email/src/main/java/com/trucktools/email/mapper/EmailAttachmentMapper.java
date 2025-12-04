package com.trucktools.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trucktools.email.entity.EmailAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 邮件附件Mapper
 */
@Mapper
public interface EmailAttachmentMapper extends BaseMapper<EmailAttachment> {

    /**
     * 根据模板ID获取附件列表
     */
    @Select("SELECT * FROM t_email_attachment WHERE template_id = #{templateId} AND deleted = 0 AND status = 1")
    List<EmailAttachment> selectByTemplateId(@Param("templateId") Long templateId);
}

