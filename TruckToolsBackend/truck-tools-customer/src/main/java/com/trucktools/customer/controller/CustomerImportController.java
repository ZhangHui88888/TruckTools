package com.trucktools.customer.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.trucktools.common.core.domain.Result;
import com.trucktools.common.utils.SecurityUtils;
import com.trucktools.customer.dto.*;
import com.trucktools.customer.service.CustomerImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 客户导入控制器
 */
@Slf4j
@Tag(name = "客户导入", description = "Excel批量导入客户")
@RestController
@RequestMapping("/api/v1/customers/import")
@RequiredArgsConstructor
public class CustomerImportController {

    private final CustomerImportService customerImportService;

    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("客户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=utf-8''" + fileName + ".xlsx");

        // 模板数据
        List<CustomerImportTemplate> templateData = List.of(
                CustomerImportTemplate.builder()
                        .name("张三（必填）")
                        .email("zhangsan@example.com（必填）")
                        .phone("+86 13800138000")
                        .company("ABC贸易有限公司")
                        .position("采购经理")
                        .country("美国")
                        .website("https://www.example.com")
                        .address("123 Main Street, New York")
                        .meetingTime("2025-01-15 14:00:00")
                        .meetingLocation("广交会A馆3楼")
                        .priority("1（1=高，2=中，3=低）")
                        .remark("对我司产品非常感兴趣")
                        .build(),
                CustomerImportTemplate.builder()
                        .name("John Smith")
                        .email("john@company.com")
                        .phone("+1 555-1234")
                        .company("Smith Industries")
                        .position("CEO")
                        .country("英国")
                        .website("https://smith.com")
                        .address("456 Oxford Street, London")
                        .meetingTime("")
                        .meetingLocation("")
                        .priority("2")
                        .remark("")
                        .build()
        );

        EasyExcel.write(response.getOutputStream(), CustomerImportTemplate.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("客户数据")
                .doWrite(templateData);
    }

    @Operation(summary = "上传Excel文件")
    @PostMapping("/upload")
    public Result<ImportUploadResult> uploadFile(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        ImportUploadResult result = customerImportService.uploadAndParse(userId, file);
        return Result.success(result);
    }

    @Operation(summary = "确认字段映射并预检")
    @PostMapping("/{importId}/validate")
    public Result<ImportValidationResult> validate(
            @PathVariable String importId,
            @RequestBody ImportValidateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ImportValidationResult result = customerImportService.validate(userId, importId, request);
        return Result.success(result);
    }

    @Operation(summary = "执行导入")
    @PostMapping("/{importId}/execute")
    public Result<ImportStatusResult> execute(
            @PathVariable String importId,
            @RequestBody ImportExecuteRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        customerImportService.execute(userId, importId, request);
        
        // 同步执行完成，返回最终状态
        ImportStatusResult result = customerImportService.getStatus(userId, importId);
        return Result.success("导入完成", result);
    }

    @Operation(summary = "查询导入状态")
    @GetMapping("/{importId}/status")
    public Result<ImportStatusResult> getStatus(@PathVariable String importId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ImportStatusResult result = customerImportService.getStatus(userId, importId);
        return Result.success(result);
    }

    /**
     * 导入模板类
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CustomerImportTemplate {
        @com.alibaba.excel.annotation.ExcelProperty("客户姓名")
        private String name;

        @com.alibaba.excel.annotation.ExcelProperty("邮箱")
        private String email;

        @com.alibaba.excel.annotation.ExcelProperty("手机号")
        private String phone;

        @com.alibaba.excel.annotation.ExcelProperty("所属公司")
        private String company;

        @com.alibaba.excel.annotation.ExcelProperty("职位")
        private String position;

        @com.alibaba.excel.annotation.ExcelProperty("国家")
        private String country;

        @com.alibaba.excel.annotation.ExcelProperty("公司官网")
        private String website;

        @com.alibaba.excel.annotation.ExcelProperty("地址")
        private String address;

        @com.alibaba.excel.annotation.ExcelProperty("会面时间")
        private String meetingTime;

        @com.alibaba.excel.annotation.ExcelProperty("会面地点")
        private String meetingLocation;

        @com.alibaba.excel.annotation.ExcelProperty("优先级")
        private String priority;

        @com.alibaba.excel.annotation.ExcelProperty("备注")
        private String remark;
    }
}
