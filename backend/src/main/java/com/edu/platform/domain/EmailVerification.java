package com.edu.platform.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailVerification {
    private Long verificationId;
    private Long userId;
    private String email;
    private String token;
    private String purpose; // SIGNUP, PASSWORD_RESET
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
}
