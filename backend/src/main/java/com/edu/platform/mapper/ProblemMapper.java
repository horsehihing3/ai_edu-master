package com.edu.platform.mapper;

import com.edu.platform.domain.Problem;
import com.edu.platform.domain.ProblemOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface ProblemMapper {
    Optional<Problem> findById(@Param("problemId") Long problemId);
    List<Problem> search(@Param("level") String level, @Param("grade") String grade,
                         @Param("unitName") String unitName, @Param("schoolId") Long schoolId,
                         @Param("keyword") String keyword,
                         @Param("offset") int offset, @Param("limit") int limit);
    long countSearch(@Param("level") String level, @Param("grade") String grade,
                     @Param("unitName") String unitName, @Param("schoolId") Long schoolId,
                     @Param("keyword") String keyword);
    List<Problem> findForDiagnosis(@Param("level") String level, @Param("count") int count);
    List<Problem> findByAssignmentId(@Param("assignmentId") Long assignmentId);
    List<ProblemOption> findOptionsByProblemId(@Param("problemId") Long problemId);
    void insert(Problem problem);
    void update(Problem problem);
    void deleteById(@Param("problemId") Long problemId);
    void insertOption(ProblemOption option);
    void deleteOptionsByProblemId(@Param("problemId") Long problemId);
    long count();
    List<Map<String, Object>> getWeakUnitStats();

    // 어드민 전용 — approval_status 필터 포함, is_active 무관
    List<Problem> searchAdmin(@Param("approvalStatus") String approvalStatus,
                              @Param("level") String level,
                              @Param("keyword") String keyword,
                              @Param("offset") int offset, @Param("limit") int limit);
    long countSearchAdmin(@Param("approvalStatus") String approvalStatus,
                          @Param("level") String level,
                          @Param("keyword") String keyword);

    void updateApprovalStatus(@Param("problemId") Long problemId,
                              @Param("approvalStatus") String approvalStatus,
                              @Param("reviewedBy") Long reviewedBy,
                              @Param("rejectReason") String rejectReason);

    // [2026-03-26] 중복 문제 감지 — question_text 완전 일치 시 기존 problem_id 반환 (null이면 중복 없음)
    Long findIdByQuestionText(@Param("questionText") String questionText);
}
