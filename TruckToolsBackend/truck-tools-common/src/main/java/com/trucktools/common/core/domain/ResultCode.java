package com.trucktools.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),
    
    // 通用错误 400-499
    FAIL(400, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权或Token已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // 服务器错误 500-599
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 认证相关 1001-1999
    USER_NOT_FOUND(1001, "用户名或密码错误"),
    TOKEN_EXPIRED(1002, "Token已过期"),
    TOKEN_INVALID(1003, "无效的Token"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    USERNAME_EXISTS(1005, "用户名已存在"),
    EMAIL_EXISTS(1006, "邮箱已存在"),
    PASSWORD_ERROR(1007, "原密码错误"),
    EMAIL_NOT_VERIFIED(1008, "邮箱未验证"),
    
    // 客户模块 2001-2999
    CUSTOMER_NOT_FOUND(2001, "客户不存在"),
    CUSTOMER_EMAIL_EXISTS(2002, "客户邮箱已存在"),
    IMPORT_FILE_ERROR(2003, "导入文件格式错误"),
    IMPORT_DATA_ERROR(2004, "导入数据错误"),
    BUSINESS_CARD_NOT_FOUND(2005, "名片记录不存在"),
    OCR_FAILED(2006, "OCR识别失败"),
    
    // 邮件模块 3001-3999
    SMTP_CONFIG_ERROR(3001, "SMTP配置错误"),
    EMAIL_SEND_FAILED(3002, "邮件发送失败"),
    EMAIL_LIMIT_EXCEEDED(3003, "超出发送限制"),
    TEMPLATE_NOT_FOUND(3004, "邮件模板不存在"),
    TASK_NOT_FOUND(3005, "发送任务不存在"),
    TASK_STATUS_ERROR(3006, "任务状态错误"),
    
    // OCR模块 4001-4999
    OCR_SERVICE_ERROR(4001, "OCR服务异常"),
    IMAGE_FORMAT_ERROR(4002, "图片格式不支持"),
    IMAGE_SIZE_ERROR(4003, "图片大小超限"),
    
    // 系统模块 5001-5999
    FILE_UPLOAD_ERROR(5001, "文件上传失败"),
    FILE_NOT_FOUND(5002, "文件不存在"),
    CONFIG_ERROR(5003, "系统配置错误");

    private final Integer code;
    private final String message;
}

