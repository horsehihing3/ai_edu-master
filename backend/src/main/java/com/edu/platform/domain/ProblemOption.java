package com.edu.platform.domain;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProblemOption {
    private Long optionId;
    private Long problemId;
    private Integer optionNo;
    private String optionText;
    private String optionImgUrl;
}
