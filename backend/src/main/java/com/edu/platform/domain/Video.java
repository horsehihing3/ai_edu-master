package com.edu.platform.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Video {
    private Long videoId;
    private Long problemId;
    private String title;
    private String subject;
    private String videoType;
    private String videoUrl;
    private String cdnUrl;
    private String thumbnailUrl;
    private String subtitleUrl;
    private Integer durationSec;
    private BigDecimal fileSizeMb;
    private Long schoolId;
    private String grade;
    private String level;
    private String unitName;
    private Integer viewCount;
    private Boolean isActive;
    // problems 테이블에서 JOIN
    private String questionText;
    private String explanation;
    private Long uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
