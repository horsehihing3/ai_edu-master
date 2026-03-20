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
    long count();
    List<Map<String, Object>> getWeakUnitStats();
}
