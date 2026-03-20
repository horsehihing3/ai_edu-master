package com.edu.platform.dto.teacher;

import com.edu.platform.domain.Assignment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCreateRequest {

    @NotBlank(message = "과제 제목을 입력해주세요.")
    private String title;

    private String description;

    @NotNull(message = "대상 유형을 선택해주세요.")
    private Assignment.TargetType targetType;

    private Assignment.TargetLevel targetLevel;

    private List<Long> problemIds;

    private List<Long> classIds;

    private List<Long> studentIds;

    private LocalDateTime dueDate;

    private Boolean notifyEmail;

    private Boolean isAutoAssign;
}
