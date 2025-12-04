package com.trucktools.product.service;

import com.trucktools.product.dto.ImportResultDTO;
import com.trucktools.product.dto.ImportStatusDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel导入服务接口
 */
public interface ExcelImportService {

    /**
     * 上传并解析Excel文件
     * @param userId 用户ID
     * @param file Excel文件
     * @return 解析结果(预览)
     */
    ImportResultDTO uploadAndParse(Long userId, MultipartFile file);

    /**
     * 执行导入
     * @param userId 用户ID
     * @param importId 导入ID
     * @return 导入状态
     */
    ImportStatusDTO executeImport(Long userId, String importId);

    /**
     * 获取导入状态
     * @param userId 用户ID
     * @param importId 导入ID
     * @return 导入状态
     */
    ImportStatusDTO getImportStatus(Long userId, String importId);

    /**
     * 下载产品导入模板
     * @param response HTTP响应
     */
    void downloadTemplate(HttpServletResponse response);
}

