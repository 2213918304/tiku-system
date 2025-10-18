package com.springboot.tiku.dto.question;

import com.springboot.tiku.entity.Question;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 题目请求DTO
 */
@Data
public class QuestionRequest {
    
    @NotNull(message = "学科ID不能为空")
    private Long subjectId;
    
    private Long chapterId;
    
    @NotNull(message = "题型不能为空")
    private Question.QuestionType type;
    
    @NotBlank(message = "题目标题不能为空")
    private String title;
    
    private String content;
    
    private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;
    
    private BigDecimal score = BigDecimal.valueOf(5.0);
    
    /**
     * 选项（JSON格式）
     * 单选/多选：["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"]
     * 匹配题：{"left": ["概念1", "概念2"], "right": ["解释1", "解释2"]}
     * 排序题：["选项1", "选项2", "选项3"]
     */
    private Object options;
    
    /**
     * 答案（JSON格式）
     * 单选：{"answer": "A"}
     * 多选：{"answer": ["A", "B", "C"]}
     * 判断：{"answer": true}
     * 填空：{"answer": ["答案1", "答案2"], "keywords": ["关键词1", "关键词2"]}
     * 主观题：{"answer": "参考答案", "keywords": ["关键词1", "关键词2"], "points": ["要点1", "要点2"]}
     * 匹配题：{"answer": {"1": "A", "2": "B"}}
     * 排序题：{"answer": [0, 2, 1, 3]}
     */
    @NotNull(message = "答案不能为空")
    private Object answer;
    
    private String answerAnalysis;
    
    private Boolean aiGradingEnabled = false;
    
    private Map<String, Object> aiGradingConfig;
    
    /**
     * 评分标准（主观题）
     * [
     *   {"dimension": "要点完整性", "score": 40, "description": "..."},
     *   {"dimension": "准确性", "score": 30, "description": "..."}
     * ]
     */
    private Object scoringCriteria;
    
    private Object referenceKeywords;
    
    private String tags;
    
    private String knowledgePoints;
    
    private Integer status = 1;
}



