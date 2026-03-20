package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SystemCode {

    private Long codeId;
    private String codeGroup;
    private String codeValue;
    private String codeName;
    private Long parentCodeId;
    private Integer sortOrder;
    private Boolean isActive;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조회 시 부모/자식 정보
    private String parentCodeName;
    private List<SystemCode> children;
}
