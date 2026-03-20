package com.edu.platform.dto.student;

import lombok.*;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class WrongNoteDto {
    private Long wrongNoteId;
    private Long problemId;
    private String subject;
    private String level;
    private String unitName;
    private String questionText;
    private String memo;
    private Boolean isResolved;
    private String createdAt;
}
