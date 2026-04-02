package com.edu.platform.mapper;

import com.edu.platform.domain.LearningSession;
import com.edu.platform.domain.ProblemAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface LearningSessionMapper {
    Optional<LearningSession> findById(@Param("sessionId") Long sessionId);
    List<LearningSession> findByStudentId(@Param("studentId") Long studentId,
                                           @Param("offset") int offset, @Param("limit") int limit);
    List<LearningSession> findAllByStudentId(@Param("studentId") Long studentId);
    List<LearningSession> findLatestByStudentId(@Param("studentId") Long studentId,
                                                 @Param("offset") int offset, @Param("limit") int limit);
    Optional<LearningSession> findInProgressByStudentId(@Param("studentId") Long studentId);
    Optional<LearningSession> findLatestByStudentAndAssignment(@Param("studentId") Long studentId,
                                                                @Param("assignmentId") Long assignmentId);
    Optional<LearningSession> findInProgressByStudentAndAssignment(@Param("studentId") Long studentId,
                                                                    @Param("assignmentId") Long assignmentId);
    void insert(LearningSession session);
    void update(LearningSession session);
    void updateStatus(@Param("sessionId") Long sessionId, @Param("status") String status);
    void insertAttempt(ProblemAttempt attempt);
    void updateAttemptFeedback(@Param("sessionId") Long sessionId,
                               @Param("problemId") Long problemId,
                               @Param("feedbackLike") String feedbackLike);
    Optional<ProblemAttempt> findAttempt(@Param("sessionId") Long sessionId,
                                          @Param("problemId") Long problemId);
    List<ProblemAttempt> findAttemptsBySession(@Param("sessionId") Long sessionId);
    long countCorrectAttemptsByStudentId(@Param("studentId") Long studentId, @Param("isCorrect") boolean isCorrect);
    List<Map<String, Object>> getDailySessionCounts(@Param("days") int days);
    List<Map<String, Object>> getDailySolvedCounts(@Param("days") int days);

    // [2026-04-02] 대시보드 KPI 실데이터
    long countDauToday();
    long countDauYesterday();
    long countMauThisMonth();
    long countMauLastMonth();
    long sumDailySolvedToday();
    long sumDailySolvedYesterday();
}
