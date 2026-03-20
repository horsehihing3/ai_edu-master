package com.edu.platform.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSubmitRequest {

    @NotNull(message = "문제 ID를 입력해주세요.")
    private Long problemId;

    private String submittedAnswer;

    private Integer timeSpentSec;

    private Boolean requestHint;

    private Boolean requestVideo;
}
