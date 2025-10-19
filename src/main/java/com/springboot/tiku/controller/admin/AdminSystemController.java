package com.springboot.tiku.controller.admin;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.system.SystemInfoDTO;
import com.springboot.tiku.dto.system.SystemSettingsDTO;
import com.springboot.tiku.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员 - 系统设置控制器
 */
@Tag(name = "管理员-系统设置", description = "系统配置和信息管理")
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemController {
    
    private final SystemConfigService systemConfigService;
    
    /**
     * 获取所有设置
     */
    @Operation(summary = "获取所有设置")
    @GetMapping("/settings")
    public Result<SystemSettingsDTO> getAllSettings() {
        SystemSettingsDTO settings = systemConfigService.getAllSettings();
        return Result.success(settings);
    }
    
    /**
     * 保存基本设置
     */
    @Operation(summary = "保存基本设置")
    @PostMapping("/settings/basic")
    public Result<Void> saveBasicSettings(@RequestBody SystemSettingsDTO.BasicSettings settings) {
        systemConfigService.saveBasicSettings(settings);
        return Result.success();
    }
    
    /**
     * 保存答题设置
     */
    @Operation(summary = "保存答题设置")
    @PostMapping("/settings/practice")
    public Result<Void> savePracticeSettings(@RequestBody SystemSettingsDTO.PracticeSettings settings) {
        systemConfigService.savePracticeSettings(settings);
        return Result.success();
    }
    
    /**
     * 保存AI设置
     */
    @Operation(summary = "保存AI设置")
    @PostMapping("/settings/ai")
    public Result<Void> saveAISettings(@RequestBody SystemSettingsDTO.AISettings settings) {
        systemConfigService.saveAISettings(settings);
        return Result.success();
    }
    
    /**
     * 保存安全设置
     */
    @Operation(summary = "保存安全设置")
    @PostMapping("/settings/security")
    public Result<Void> saveSecuritySettings(@RequestBody SystemSettingsDTO.SecuritySettings settings) {
        systemConfigService.saveSecuritySettings(settings);
        return Result.success();
    }
    
    /**
     * 获取系统信息
     */
    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public Result<SystemInfoDTO> getSystemInfo() {
        SystemInfoDTO info = systemConfigService.getSystemInfo();
        return Result.success(info);
    }
    
    /**
     * 清空答题记录
     */
    @Operation(summary = "清空答题记录")
    @PostMapping("/data/clear-answer-records")
    public Result<Map<String, Object>> clearAnswerRecords() {
        Map<String, Object> result = systemConfigService.clearAnswerRecords();
        return Result.success("答题记录已清空", result);
    }
    
    /**
     * 备份数据库
     */
    @Operation(summary = "备份数据库")
    @PostMapping("/data/backup")
    public Result<Map<String, Object>> backupDatabase() {
        Map<String, Object> result = systemConfigService.backupDatabase();
        return Result.success("数据库备份成功", result);
    }
    
    /**
     * 重建索引
     */
    @Operation(summary = "重建索引")
    @PostMapping("/data/rebuild-index")
    public Result<Map<String, Object>> rebuildIndex() {
        Map<String, Object> result = systemConfigService.rebuildIndex();
        return Result.success("索引重建成功", result);
    }
    
    /**
     * 测试AI连接
     */
    @Operation(summary = "测试AI连接")
    @PostMapping("/test-ai")
    public Result<Void> testAI() {
        // TODO: 实现AI连接测试
        return Result.success();
    }
}

