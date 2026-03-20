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
}
