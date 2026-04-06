package com.edu.platform.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class ClassDto {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClassResponse {
        private Long classId;
        private String className;
        private String grade;
        private String levelFilter;
        private String inviteCode;
        private int studentCount;
        private LocalDateTime createdAt;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class JoinRequest {
        @NotBlank(message = "초대코드를 입력해주세요.")
        private String inviteCode;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class ClassCreateRequest {
        @NotBlank(message = "학급 이름을 입력해주세요.")
        private String className;
        private String grade;
        private String levelFilter;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class ClassUpdateRequest {
        private String className;
        private String grade;
        private String levelFilter;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class AddMembersRequest {
        private java.util.List<Long> studentIds;
    }

    // [2026-04-06] 학생 학급 이동 요청
    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class MoveRequest {
        private Long targetClassId;
    }

    // [2026-04-06] 어드민용 학생 재배정 요청
    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class ReassignRequest {
        private Long fromClassId;
        private Long toClassId;
    }
}
