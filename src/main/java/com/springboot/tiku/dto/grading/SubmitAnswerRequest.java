package com.springboot.tiku.dto.grading;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交答案请求DTO
 */
@Data
public class SubmitAnswerRequest {
    
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    
    /**
     * 用户答案（JSON格式）
     * 单选：{"answer": "A"}
     * 多选：{"answer": ["A", "B", "C"]}
     * 判断：{"answer": true}
     * 填空：{"answer": ["答案1", "答案2"]}
     * 主观题：{"answer": "学生的回答内容..."}
     */
    @NotNull(message = "答案不能为空")
    private Object userAnswer;
    
    /**
     * 练习模式
     */
    private String practiceMode;
    
    /**
     * 考试ID（如果是考试）
     */
    private Long examId;
    
    /**
     * 答题用时（秒）
     */
    private Integer timeSpent;
}



