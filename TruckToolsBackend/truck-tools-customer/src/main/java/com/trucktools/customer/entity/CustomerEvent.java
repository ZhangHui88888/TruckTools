package com.trucktools.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trucktools.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客户事件实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_customer_event")
@Schema(description = "客户事件")
public class CustomerEvent extends BaseEntity {

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "事件地点")
    private String eventLocation;

    @Schema(description = "事件内容")
    private String eventContent;

    @Schema(description = "事件进度状态: pending_customer=等待客户回复, pending_us=等待我们回复")
    private String eventStatus;

    @Schema(description = "是否系统自动生成: 0=否, 1=是")
    private Integer isSystemGenerated;

    @Schema(description = "关联的父事件ID")
    private Long parentEventId;

    @Schema(description = "附件URL列表(JSON格式)")
    private String attachmentUrls;
}

