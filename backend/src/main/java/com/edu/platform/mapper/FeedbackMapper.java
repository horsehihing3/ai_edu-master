package com.edu.platform.mapper;

import com.edu.platform.domain.AssignmentFeedback;
import com.edu.platform.domain.AttemptFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface FeedbackMapper {

    // 과제 전체 피드백
    void insertAssignmentFeedback(AssignmentFeedback feedback);
    void updateAssignmentFeedback(AssignmentFeedback feedback);
    Optional<AssignmentFeedback> findAssignmentFeedback(@Param("assignmentId") Long assignmentId,
                                                         @Param("studentId") Long studentId);

    // 문제별 피드백
    void insertAttemptFeedback(AttemptFeedback feedback);
    void updateAttemptFeedback(AttemptFeedback feedback);
    Optional<AttemptFeedback> findAttemptFeedback(@Param("attemptId") Long attemptId);
    List<AttemptFeedback> findAttemptFeedbacksByAssignment(@Param("assignmentId") Long assignmentId,
                                                            @Param("studentId") Long studentId);

    // 학생별 문제 풀이 상세 (과제 기준)
    List<Map<String, Object>> getStudentAttemptDetails(@Param("assignmentId") Long assignmentId,
                                                        @Param("studentId") Long studentId);
}
