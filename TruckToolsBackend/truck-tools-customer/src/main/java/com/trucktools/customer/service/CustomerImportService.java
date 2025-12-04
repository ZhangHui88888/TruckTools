package com.trucktools.customer.service;

import com.trucktools.customer.dto.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 客户导入服务接口
 */
public interface CustomerImportService {

    /**
     * 上传Excel文件并解析
     * @param userId 用户ID
     * @param file Excel文件
     * @return 解析结果
     */
    ImportUploadResult uploadAndParse(Long userId, MultipartFile file);

    /**
     * 验证导入数据
     * @param userId 用户ID
     * @param importId 导入任务ID
     * @param request 验证请求
     * @return 验证结果
     */
    ImportValidationResult validate(Long userId, String importId, ImportValidateRequest request);

    /**
     * 执行导入
     * @param userId 用户ID
     * @param importId 导入任务ID
     * @param request 执行请求
     */
    void execute(Long userId, String importId, ImportExecuteRequest request);

    /**
     * 查询导入状态
     * @param userId 用户ID
     * @param importId 导入任务ID
     * @return 导入状态
     */
    ImportStatusResult getStatus(Long userId, String importId);
}

