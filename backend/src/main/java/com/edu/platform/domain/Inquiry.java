package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Inquiry {
    private Long inquiryId;
    private Long userId;
    private String category;
    private String title;
    private String content;
    private String attachmentUrl;
    private String status;
    private Boolean isSecret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
