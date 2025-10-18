package com.springboot.tiku.config;

import com.springboot.tiku.entity.User;
import com.springboot.tiku.repository.UserRepository;
import com.springboot.tiku.service.DataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化
 * 系统启动时自动执行
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitRunner implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataImportService dataImportService;
    
    @Value("${app.admin.username:admin}")
    private String adminUsername;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    @Value("${app.admin.email:admin@tiku.com}")
    private String adminEmail;
    
    @Override
    public void run(String... args) {
        log.info("================== 数据初始化开始 ==================");
        
        // 1. 创建默认管理员账号
        createAdminUser();
        
        // 2. 检查是否需要导入题库
        // checkAndImportQuestions();
        
        log.info("================== 数据初始化完成 ==================");
    }
    
    /**
     * 创建默认管理员账号
     */
    private void createAdminUser() {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setRealName("系统管理员");
            admin.setRole(User.UserRole.ADMIN);
            admin.setStatus(1);
            
            userRepository.save(admin);
            log.info("✓ 默认管理员账号创建成功");
            log.info("  用户名: {}", adminUsername);
            log.info("  密码: {}", adminPassword);
            log.warn("  ⚠️ 请登录后立即修改密码！");
        } else {
            log.info("✓ 管理员账号已存在，跳过创建");
        }
    }
    
    /**
     * 检查并导入题库
     * 注释掉此方法，避免每次启动都导入
     * 可通过API手动触发导入：POST /api/admin/data-import/mayuan
     */
    private void checkAndImportQuestions() {
        try {
            // 检查是否已有题目数据
            // 如果没有，则自动导入
            log.info("检查题库数据...");
            
            // TODO: 判断是否需要导入
            // 首次启动时可以自动导入，后续启动跳过
            
            log.info("题库数据检查完成");
        } catch (Exception e) {
            log.error("题库检查失败", e);
        }
    }
}



