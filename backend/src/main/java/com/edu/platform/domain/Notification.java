package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {
    private Long notificationId;
    private Long userId;
    private Long senderId;
    private String notiType;
    private String title;
    private String content;
    private String linkUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
