package com.edu.platform.dto.student;

import com.edu.platform.domain.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentHomeDto {

    private String userName;
    private Student.StudentLevel studentLevel;
    private List<AssignmentSummaryDto> currentAssignments;
    private RecentProgressDto recentProgress;
    private Integer totalSolvedCount;
    private Integer weeklyGoal;
    private Integer weeklyProgress;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentSummaryDto {
        private Long assignmentId;
        private String title;
        private String dueDate;
        private Integer progress;
        private Integer totalProblems;
        private Boolean isCompleted;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentProgressDto {
        private Integer totalSolved;
        private Integer correctCount;
        private Double accuracy;
        private Integer studyTimeSec;
    }
}
