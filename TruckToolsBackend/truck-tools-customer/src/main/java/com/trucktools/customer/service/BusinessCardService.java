package com.trucktools.customer.service;

import com.trucktools.customer.dto.BusinessCardVO;
import com.trucktools.customer.dto.CustomerRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 名片服务接口
 */
public interface BusinessCardService {

    /**
     * 上传名片
     */
    Map<String, Object> upload(Long userId, List<MultipartFile> files, List<Integer> priorities);

    /**
     * 获取识别结果
     */
    BusinessCardVO getResult(Long userId, Long cardId);

    /**
     * 获取批次状态
     */
    Map<String, Object> getBatchStatus(Long userId, String batchId);

    /**
     * 确认识别结果转为客户
     */
    Long confirm(Long userId, Long cardId, CustomerRequest request);

    /**
     * 批量确认转为客户
     */
    Map<String, Integer> batchConfirm(Long userId, List<Long> cardIds);

    /**
     * 处理OCR识别（异步任务）
     */
    void processOcr(Long cardId);
}

