package com.springboot.tiku.service;

import com.springboot.tiku.dto.statistics.RankingItem;
import com.springboot.tiku.entity.AnswerRecord;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.repository.AnswerRecordRepository;
import com.springboot.tiku.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排行榜服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {
    
    private final UserRepository userRepository;
    private final AnswerRecordRepository answerRecordRepository;
    
    /**
     * 获取答题数排行榜
     */
    public List<RankingItem> getAnswerCountRanking(Integer limit, Long currentUserId) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        
        List<User> users = userRepository.findByStatusOrderByIdAsc(1);
        
        // 统计每个用户的答题数（包括0的用户）
        List<RankingItem> rankings = new ArrayList<>();
        for (User user : users) {
            long answerCount = answerRecordRepository.countByUserId(user.getId());
            // 所有用户都加入排行榜，包括答题数为0的
            RankingItem item = RankingItem.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(maskName(user.getRealName()))
                    .value(answerCount)
                    .isCurrentUser(user.getId().equals(currentUserId))
                    .build();
            rankings.add(item);
        }
        
        // 按答题数排序
        rankings.sort(Comparator.comparing(RankingItem::getValue).reversed());
        
        // 设置排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * 获取正确率排行榜（至少答题10道）
     */
    public List<RankingItem> getAccuracyRanking(Integer limit, Long currentUserId) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        
        List<User> users = userRepository.findByStatusOrderByIdAsc(1);
        
        List<RankingItem> rankings = new ArrayList<>();
        for (User user : users) {
            long totalAnswered = answerRecordRepository.countByUserId(user.getId());
            
            // 至少答题10道才进入排行榜
            if (totalAnswered >= 10) {
                long correctCount = answerRecordRepository.countByUserIdAndIsCorrect(user.getId(), true);
                
                BigDecimal accuracy = BigDecimal.valueOf(correctCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalAnswered), 2, RoundingMode.HALF_UP);
                
                RankingItem item = RankingItem.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .realName(maskName(user.getRealName()))
                        .value(totalAnswered)
                        .accuracy(accuracy)
                        .isCurrentUser(user.getId().equals(currentUserId))
                        .build();
                rankings.add(item);
            }
        }
        
        // 按正确率排序，正确率相同则按答题数排序
        rankings.sort(Comparator
                .comparing(RankingItem::getAccuracy).reversed()
                .thenComparing(RankingItem::getValue, Comparator.reverseOrder()));
        
        // 设置排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * 获取积分排行榜
     */
    public List<RankingItem> getPointsRanking(Integer limit, Long currentUserId) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        
        List<User> users = userRepository.findByStatusOrderByIdAsc(1);
        
        List<RankingItem> rankings = new ArrayList<>();
        for (User user : users) {
            long correctCount = answerRecordRepository.countByUserIdAndIsCorrect(user.getId(), true);
            
            // 简单积分规则：正确题数 * 10
            int points = (int) (correctCount * 10);
            
            // 所有用户都加入排行榜，包括积分为0的
            RankingItem item = RankingItem.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(maskName(user.getRealName()))
                    .points(points)
                    .value(correctCount)
                    .isCurrentUser(user.getId().equals(currentUserId))
                    .build();
            rankings.add(item);
        }
        
        // 按积分排序
        rankings.sort(Comparator.comparing(RankingItem::getPoints).reversed());
        
        // 设置排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * 获取学科排行榜
     */
    public List<RankingItem> getSubjectRanking(Long subjectId, Integer limit, Long currentUserId) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        
        List<User> users = userRepository.findByStatusOrderByIdAsc(1);
        
        List<RankingItem> rankings = new ArrayList<>();
        for (User user : users) {
            long answerCount = answerRecordRepository
                    .countByUserIdAndQuestionSubjectId(user.getId(), subjectId);
            
            if (answerCount > 0) {
                long correctCount = answerRecordRepository
                        .countByUserIdAndQuestionSubjectIdAndIsCorrect(user.getId(), subjectId, true);
                
                BigDecimal accuracy = BigDecimal.valueOf(correctCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(answerCount), 2, RoundingMode.HALF_UP);
                
                RankingItem item = RankingItem.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .realName(maskName(user.getRealName()))
                        .value(answerCount)
                        .accuracy(accuracy)
                        .isCurrentUser(user.getId().equals(currentUserId))
                        .build();
                rankings.add(item);
            }
        }
        
        // 按答题数排序
        rankings.sort(Comparator.comparing(RankingItem::getValue).reversed());
        
        // 设置排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * 隐藏真实姓名部分字符（隐私保护）
     */
    private String maskName(String realName) {
        if (realName == null || realName.length() <= 1) {
            return realName;
        }
        
        if (realName.length() == 2) {
            return realName.charAt(0) + "*";
        }
        
        // 保留首尾，中间用*代替
        StringBuilder masked = new StringBuilder();
        masked.append(realName.charAt(0));
        for (int i = 1; i < realName.length() - 1; i++) {
            masked.append("*");
        }
        masked.append(realName.charAt(realName.length() - 1));
        
        return masked.toString();
    }
}



