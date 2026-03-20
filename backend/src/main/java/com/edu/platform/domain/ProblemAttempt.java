package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProblemAttempt {
    private Long attemptId;
    private Long sessionId;
    private Long studentId;
    private Long problemId;
    private String submittedAnswer;
    private Boolean isCorrect;
    private Integer timeSpentSec;
    private Boolean isBookmarked;
    private Boolean requestedVideo;
    private String feedbackLike;
    private LocalDateTime attemptedAt;
}
