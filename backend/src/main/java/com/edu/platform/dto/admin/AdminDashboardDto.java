package com.edu.platform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {

    private Long totalStudents;
    private Long totalTeachers;
    private Long totalSchools;
    private Long dau;
    private Long mau;
    private Long dailySolvedCount;
    private Long totalProblems;
    private Long activeUsers;
    private Double averageAccuracy;
}
