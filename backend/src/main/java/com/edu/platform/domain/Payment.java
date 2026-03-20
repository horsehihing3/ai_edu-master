package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {
    private Long paymentId;
    private Long userId;
    private Long subscriptionId;
    private String pgProvider;
    private String pgTid;
    private String merchantUid;
    private Integer amount;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String refundReason;
    private LocalDateTime createdAt;

    // JOIN 필드
    private String userName;
    private String userEmail;
    private String planName;
    private String planType;
}
