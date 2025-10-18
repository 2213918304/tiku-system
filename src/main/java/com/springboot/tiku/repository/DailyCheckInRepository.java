package com.springboot.tiku.repository;

import com.springboot.tiku.entity.DailyCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 每日打卡Repository
 */
@Repository
public interface DailyCheckInRepository extends JpaRepository<DailyCheckIn, Long>, JpaSpecificationExecutor<DailyCheckIn> {
    
    /**
     * 查询用户某天的打卡记录
     */
    Optional<DailyCheckIn> findByUserIdAndCheckDate(Long userId, LocalDate checkDate);
    
    /**
     * 查询用户的打卡历史
     */
    List<DailyCheckIn> findByUserIdOrderByCheckDateDesc(Long userId);
    
    /**
     * 查询用户最近的打卡记录
     */
    Optional<DailyCheckIn> findFirstByUserIdOrderByCheckDateDesc(Long userId);
    
    /**
     * 统计用户总打卡天数
     */
    long countByUserId(Long userId);
    
    /**
     * 检查某天是否已打卡
     */
    boolean existsByUserIdAndCheckDate(Long userId, LocalDate checkDate);
    
    /**
     * 查询时间范围内的打卡记录
     */
    List<DailyCheckIn> findByUserIdAndCheckDateBetween(Long userId, LocalDate start, LocalDate end);
}


