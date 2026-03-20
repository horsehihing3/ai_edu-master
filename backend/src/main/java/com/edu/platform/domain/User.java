package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User {

    public enum UserRole {
        STUDENT, TEACHER, SUPER_USER, ADMIN
    }

    private Long userId;
    private String email;
    private String passwordHash;
    private String name;
    private String role;
    private Long schoolId;
    private String phone;
    private String profileImgUrl;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Boolean notiAssignment;
    private Boolean notiAnnouncement;
    private Boolean notiInquiryReply;

    public UserRole getRoleAsEnum() {
        return role != null ? UserRole.valueOf(role) : null;
    }
}
