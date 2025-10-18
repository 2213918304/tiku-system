package com.springboot.tiku.service.grading;

import com.springboot.tiku.dto.grading.GradingResult;
import com.springboot.tiku.entity.Question;

/**
 * 判题策略接口
 */
public interface GradingStrategy {
    
    /**
     * 判题
     * @param question 题目
     * @param userAnswer 用户答案
     * @return 判题结果
     */
    GradingResult grade(Question question, Object userAnswer);
    
    /**
     * 是否支持该题型
     * @param questionType 题型
     * @return 是否支持
     */
    boolean supports(Question.QuestionType questionType);
}



