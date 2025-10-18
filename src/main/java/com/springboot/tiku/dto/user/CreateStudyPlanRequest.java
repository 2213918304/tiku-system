package com.springboot.tiku.dto.user;

import lombok.Data;

@Data
public class CreateStudyPlanRequest {
    private Long subjectId;
    private String description;
    private Integer targetCount;
    private Integer totalDays;
}



