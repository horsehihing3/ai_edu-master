package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InquiryReply {
    private Long replyId;
    private Long inquiryId;
    private Long replierId;
    private String content;
    private LocalDateTime createdAt;
}
