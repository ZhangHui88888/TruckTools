package com.trucktools.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 处理事件请求
 */
@Data
@Schema(description = "处理事件请求")
public class ProcessEventRequest {

    @NotNull(message = "事件ID不能为空")
    @Schema(description = "要处理的事件ID")
    private String eventId;

    @NotBlank(message = "处理内容不能为空")
    @Schema(description = "处理内容")
    private String processContent;

    @Schema(description = "处理时间，默认当前时间")
    private String processTime;
    
    /**
     * 获取事件ID的Long值
     */
    public Long getEventIdAsLong() {
        return eventId != null ? Long.parseLong(eventId) : null;
    }
    
    /**
     * 获取处理时间的LocalDateTime值
     */
    public LocalDateTime getProcessTimeAsLocalDateTime() {
        if (processTime == null || processTime.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(processTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Schema(description = "附件URL列表")
    private List<String> attachmentUrls;
}
