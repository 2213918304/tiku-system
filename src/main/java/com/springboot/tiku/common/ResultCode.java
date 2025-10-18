package com.springboot.tiku.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),
    
    // 服务器错误 5xx
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    
    // 业务错误 1xxx
    // 用户相关 10xx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(1003, "邮箱已被注册"),
    PASSWORD_ERROR(1004, "密码错误"),
    USER_DISABLED(1005, "账号已被禁用"),
    TOKEN_EXPIRED(1006, "登录已过期，请重新登录"),
    TOKEN_INVALID(1007, "无效的token"),
    
    // 题目相关 20xx
    QUESTION_NOT_FOUND(2001, "题目不存在"),
    QUESTION_TYPE_NOT_SUPPORT(2002, "不支持的题型"),
    ANSWER_REQUIRED(2003, "答案不能为空"),
    
    // 学科相关 21xx
    SUBJECT_NOT_FOUND(2101, "学科不存在"),
    SUBJECT_CODE_EXISTS(2102, "学科代码已存在"),
    
    // 章节相关 22xx
    CHAPTER_NOT_FOUND(2201, "章节不存在"),
    
    // 考试相关 30xx
    EXAM_NOT_FOUND(3001, "考试不存在"),
    EXAM_NOT_STARTED(3002, "考试未开始"),
    EXAM_ENDED(3003, "考试已结束"),
    EXAM_TIMEOUT(3004, "考试时间已到"),
    EXAM_ALREADY_SUBMITTED(3005, "已提交过该考试"),
    
    // AI判题相关 40xx
    AI_GRADING_ERROR(4001, "AI判题失败"),
    AI_API_ERROR(4002, "AI接口调用失败"),
    AI_CONFIG_ERROR(4003, "AI配置错误"),
    
    // 文件相关 50xx
    FILE_UPLOAD_ERROR(5001, "文件上传失败"),
    FILE_TYPE_ERROR(5002, "文件类型不支持"),
    FILE_SIZE_ERROR(5003, "文件大小超出限制"),
    FILE_NOT_FOUND(5004, "文件不存在"),
    
    // 数据相关 60xx
    DATA_IMPORT_ERROR(6001, "数据导入失败"),
    DATA_EXPORT_ERROR(6002, "数据导出失败"),
    DATA_FORMAT_ERROR(6003, "数据格式错误");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}




