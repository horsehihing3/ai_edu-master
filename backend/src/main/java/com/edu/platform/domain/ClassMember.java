package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ClassMember {
    private Long classMemberId;
    private Long classId;
    private Long studentId;
    private LocalDateTime joinedAt;
}
