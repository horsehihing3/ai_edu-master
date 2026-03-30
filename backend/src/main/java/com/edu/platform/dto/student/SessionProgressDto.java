package com.edu.platform.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionProgressDto {

    private Long sessionId;
    private Integer solvedCount;
    private Integer totalProblems;
    private Integer correctCount;
    private Double completionRate;
    private Double accuracy;
    private String status;
    // [2026-03-30] 이어풀기 필드
    private Integer currentIndex;
    private Boolean resumed;
}
