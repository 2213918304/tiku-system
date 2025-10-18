package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.question.ImportResultDTO;
import com.springboot.tiku.dto.question.SmartImportRequest;
import com.springboot.tiku.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据导入控制器
 */
@Tag(name = "数据导入", description = "题库数据导入接口（仅管理员）")
@RestController
@RequestMapping("/admin/data-import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DataImportController {
    
    private final DataImportService dataImportService;
    
    /**
     * 导入马原题库
     */
    @Operation(summary = "导入马原题库", description = "从HTML文件导入马克思主义基本原理题库")
    @PostMapping("/mayuan")
    public Result<Map<String, Object>> importMayuanQuestions() {
        // 使用项目根目录下的HTML文件
        String htmlFilePath = "马原题库4.0.html";
        Map<String, Object> result = dataImportService.importMayuanQuestions(htmlFilePath);
        return Result.success("导入成功", result);
    }
    
    /**
     * 智能批量导入（根据Excel中的章节信息自动归类）
     */
    @Operation(summary = "智能批量导入", description = "根据Excel中的章节序号/名称自动创建章节并导入题目")
    @PostMapping("/smart-import")
    public Result<ImportResultDTO> smartImport(@RequestBody SmartImportRequest request) {
        ImportResultDTO result = dataImportService.smartImport(request);
        
        if (result.getSuccessCount() > 0) {
            return Result.success("导入完成", result);
        } else {
            return Result.error(400, "导入失败");
        }
    }
    
    /**
     * 批量导入题目（按章节导入）
     */
    @Operation(summary = "批量导入题目", description = "批量导入题目到指定学科章节")
    @PostMapping("/batch")
    public Result<ImportResultDTO> batchImport(@RequestBody SmartImportRequest request) {
        // 使用智能导入的底层方法，但是题目已经指定了学科和章节
        ImportResultDTO result = dataImportService.smartImport(request);
        
        if (result.getSuccessCount() > 0) {
            return Result.success("导入完成", result);
        } else {
            return Result.error(400, "导入失败");
        }
    }
}


