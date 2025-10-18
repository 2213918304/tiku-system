package com.springboot.tiku.dto.practice;

/**
 * 刷题模式枚举
 */
public enum PracticeMode {
    
    /**
     * 顺序刷题 - 按题目顺序依次答题
     */
    SEQUENTIAL("顺序刷题", "按题目顺序依次练习，适合系统学习"),
    
    /**
     * 随机刷题 - 随机抽取题目
     */
    RANDOM("随机刷题", "随机抽取题目，避免记忆答案位置"),
    
    /**
     * 章节练习 - 针对特定章节练习
     */
    CHAPTER("章节练习", "针对性突破薄弱章节"),
    
    /**
     * 考试模拟 - 模拟真实考试
     */
    EXAM("考试模拟", "模拟真实考试，限时答题"),
    
    /**
     * 错题强化 - 专攻错题
     */
    WRONG_QUESTION("错题强化", "专门练习错题，巩固薄弱点"),
    
    /**
     * 收藏专练 - 练习收藏的题目
     */
    FAVORITE("收藏专练", "复习收藏的重点题目"),
    
    /**
     * 闯关模式 - 游戏化闯关
     */
    CHALLENGE("闯关模式", "游戏化闯关，激励学习"),
    
    /**
     * 限时挑战 - 快速答题挑战
     */
    TIMED("限时挑战", "每题限时，训练答题速度"),
    
    /**
     * 智能推荐 - AI推荐题目
     */
    SMART_RECOMMEND("智能推荐", "根据学习数据智能推荐题目");
    
    private final String name;
    private final String description;
    
    PracticeMode(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}



