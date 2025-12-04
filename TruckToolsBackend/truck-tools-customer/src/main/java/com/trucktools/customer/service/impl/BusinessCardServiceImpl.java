package com.trucktools.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trucktools.common.core.domain.ResultCode;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.common.utils.IdGenerator;
import com.trucktools.customer.dto.BusinessCardVO;
import com.trucktools.customer.dto.CustomerRequest;
import com.trucktools.customer.entity.BusinessCard;
import com.trucktools.customer.entity.Customer;
import com.trucktools.customer.mapper.BusinessCardMapper;
import com.trucktools.customer.mapper.CustomerMapper;
import com.trucktools.customer.service.BusinessCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 名片服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessCardServiceImpl implements BusinessCardService {

    private final BusinessCardMapper businessCardMapper;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> upload(Long userId, List<MultipartFile> files, List<Integer> priorities) {
        String batchId = IdGenerator.batchNo("batch");
        List<BusinessCardVO> cards = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Integer priority = (priorities != null && i < priorities.size()) ? priorities.get(i) : 2;
            
            // TODO: 上传文件到OSS
            String imageUrl = "https://example.com/cards/" + IdGenerator.uuid() + ".jpg";
            
            BusinessCard card = new BusinessCard();
            card.setUserId(userId);
            card.setImageUrl(imageUrl);
            card.setPriority(priority);
            card.setOcrStatus(0); // 待识别
            card.setUploadBatch(batchId);
            card.setIsProcessed(0);
            card.setHasWechatQr(0);
            card.setHasWhatsappQr(0);
            
            businessCardMapper.insert(card);
            cards.add(convertToVO(card));
            
            // 异步处理OCR识别
            processOcr(card.getId());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("batchId", batchId);
        result.put("totalCount", files.size());
        result.put("cards", cards);
        
        log.info("上传名片: userId={}, batchId={}, count={}", userId, batchId, files.size());
        return result;
    }

    @Override
    public BusinessCardVO getResult(Long userId, Long cardId) {
        BusinessCard card = businessCardMapper.selectById(cardId);
        if (card == null || !card.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.BUSINESS_CARD_NOT_FOUND);
        }
        return convertToVO(card);
    }

    @Override
    public Map<String, Object> getBatchStatus(Long userId, String batchId) {
        List<BusinessCard> cards = businessCardMapper.selectByBatch(batchId);
        
        // 验证权限
        if (!cards.isEmpty() && !cards.get(0).getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        int pendingCount = 0, processingCount = 0, successCount = 0, failedCount = 0;
        List<BusinessCardVO> cardVOs = new ArrayList<>();
        
        for (BusinessCard card : cards) {
            switch (card.getOcrStatus()) {
                case 0 -> pendingCount++;
                case 1 -> processingCount++;
                case 2 -> successCount++;
                case 3 -> failedCount++;
            }
            cardVOs.add(convertToVO(card));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("batchId", batchId);
        result.put("totalCount", cards.size());
        result.put("pendingCount", pendingCount);
        result.put("processingCount", processingCount);
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("cards", cardVOs);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirm(Long userId, Long cardId, CustomerRequest request) {
        BusinessCard card = businessCardMapper.selectById(cardId);
        if (card == null || !card.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.BUSINESS_CARD_NOT_FOUND);
        }
        
        if (card.getIsProcessed() == 1) {
            throw new BusinessException("该名片已处理");
        }
        
        // 创建客户
        Customer customer = new Customer();
        customer.setUserId(userId);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCompany(request.getCompany());
        customer.setPosition(request.getPosition());
        customer.setCountry(request.getCountry());
        customer.setAddress(request.getAddress());
        customer.setWebsite(request.getWebsite());
        customer.setPriority(card.getPriority());
        
        // 处理会面时间（前端发送的是日期字符串 YYYY-MM-DD）
        if (StrUtil.isNotBlank(request.getMeetingTime())) {
            try {
                LocalDateTime meetingTime = LocalDate.parse(request.getMeetingTime()).atStartOfDay();
                customer.setMeetingTime(meetingTime);
            } catch (Exception e) {
                customer.setMeetingTime(null);
            }
        } else {
            customer.setMeetingTime(null);
        }
        
        customer.setMeetingLocation(request.getMeetingLocation());
        customer.setRemark(request.getRemark());
        customer.setSource("ocr");
        customer.setSourceFile(card.getImageUrl());
        customer.setOcrConfidence(card.getOcrConfidence());
        customer.setEmailStatus(1);
        customer.setEmailCount(0);
        
        customerMapper.insert(customer);
        
        // 更新名片状态
        card.setCustomerId(customer.getId());
        card.setIsProcessed(1);
        businessCardMapper.updateById(card);
        
        log.info("名片确认为客户: cardId={}, customerId={}", cardId, customer.getId());
        return customer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> batchConfirm(Long userId, List<Long> cardIds) {
        int successCount = 0;
        int failedCount = 0;
        
        for (Long cardId : cardIds) {
            try {
                BusinessCard card = businessCardMapper.selectById(cardId);
                if (card == null || !card.getUserId().equals(userId) || card.getIsProcessed() == 1) {
                    failedCount++;
                    continue;
                }
                
                if (card.getOcrStatus() != 2) {
                    failedCount++;
                    continue;
                }
                
                // 使用OCR识别结果创建客户
                CustomerRequest request = new CustomerRequest();
                request.setName(card.getParsedName());
                request.setEmail(card.getParsedEmail());
                request.setPhone(card.getParsedPhone());
                request.setCompany(card.getParsedCompany());
                request.setPosition(card.getParsedPosition());
                request.setAddress(card.getParsedAddress());
                request.setWebsite(card.getParsedWebsite());
                
                confirm(userId, cardId, request);
                successCount++;
            } catch (Exception e) {
                log.warn("批量确认失败: cardId={}, error={}", cardId, e.getMessage());
                failedCount++;
            }
        }
        
        Map<String, Integer> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        return result;
    }

    @Override
    @Async
    public void processOcr(Long cardId) {
        try {
            BusinessCard card = businessCardMapper.selectById(cardId);
            if (card == null) {
                return;
            }
            
            // 更新状态为识别中
            card.setOcrStatus(1);
            businessCardMapper.updateById(card);
            
            // TODO: 调用OCR服务进行识别
            // 这里模拟OCR识别结果
            simulateOcr(card);
            
            // 更新识别结果
            card.setOcrStatus(2);
            card.setOcrTime(LocalDateTime.now());
            card.setOcrProvider("mock");
            card.setOcrConfidence(new BigDecimal("95.5"));
            businessCardMapper.updateById(card);
            
            log.info("OCR识别完成: cardId={}", cardId);
        } catch (Exception e) {
            log.error("OCR识别失败: cardId={}", cardId, e);
            BusinessCard card = businessCardMapper.selectById(cardId);
            if (card != null) {
                card.setOcrStatus(3);
                card.setOcrError(e.getMessage());
                businessCardMapper.updateById(card);
            }
        }
    }
    
    /**
     * 模拟OCR识别
     */
    private void simulateOcr(BusinessCard card) {
        // 模拟识别结果
        card.setParsedName("John Smith");
        card.setParsedEmail("john.smith@example.com");
        card.setParsedPhone("+1-123-456-7890");
        card.setParsedCompany("ABC Trading Co.");
        card.setParsedPosition("Sales Manager");
        card.setParsedAddress("123 Main St, New York, NY 10001");
        card.setParsedWebsite("www.abc-trading.com");
    }

    /**
     * 转换为VO
     */
    private BusinessCardVO convertToVO(BusinessCard card) {
        BusinessCardVO vo = new BusinessCardVO();
        vo.setId(card.getId().toString());
        vo.setImageUrl(card.getImageUrl());
        vo.setThumbnailUrl(card.getImageThumbnail());
        vo.setPriority(card.getPriority());
        vo.setOcrStatus(card.getOcrStatus());
        vo.setOcrConfidence(card.getOcrConfidence() != null ? card.getOcrConfidence().doubleValue() : null);
        vo.setHasWechatQr(card.getHasWechatQr() == 1);
        vo.setHasWhatsappQr(card.getHasWhatsappQr() == 1);
        vo.setIsProcessed(card.getIsProcessed() == 1);
        vo.setCreatedAt(card.getCreatedAt());
        
        // 解析数据
        if (card.getOcrStatus() == 2) {
            BusinessCardVO.ParsedData parsedData = new BusinessCardVO.ParsedData();
            parsedData.setName(card.getParsedName());
            parsedData.setEmail(card.getParsedEmail());
            parsedData.setPhone(card.getParsedPhone());
            parsedData.setCompany(card.getParsedCompany());
            parsedData.setPosition(card.getParsedPosition());
            parsedData.setAddress(card.getParsedAddress());
            parsedData.setWebsite(card.getParsedWebsite());
            vo.setParsedData(parsedData);
        }
        
        return vo;
    }
}

