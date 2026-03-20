package com.edu.platform.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisStartResponse {

    private Long testId;
    private List<ProblemDto> problems;
    private Integer totalCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemDto {
        private Long problemId;
        private String questionText;
        private String questionImgUrl;
        private String problemType;
        private List<OptionDto> options;
        private Integer estimatedTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private Integer optionNo;
        private String optionText;
        private String optionImgUrl;
    }
}
