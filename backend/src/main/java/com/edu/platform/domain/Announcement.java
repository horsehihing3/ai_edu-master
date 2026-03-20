package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Announcement {
    private Long announcementId;
    private Long schoolId;
    private Long authorId;
    private String title;
    private String content;
    private Boolean isImportant;
    private String targetRole;
    private String imgUrl;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer viewCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
