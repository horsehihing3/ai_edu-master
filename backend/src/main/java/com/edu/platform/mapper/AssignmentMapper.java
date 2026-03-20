package com.edu.platform.mapper;

import com.edu.platform.domain.Assignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface AssignmentMapper {
    Optional<Assignment> findById(@Param("assignmentId") Long assignmentId);
    List<Assignment> findByTeacherId(@Param("teacherId") Long teacherId,
                                      @Param("offset") int offset, @Param("limit") int limit);
    long countByTeacherId(@Param("teacherId") Long teacherId);
    List<Assignment> findByStudentId(@Param("studentId") Long studentId);
    List<Assignment> findBySchoolId(@Param("schoolId") Long schoolId);
    void insert(Assignment assignment);
    void update(Assignment assignment);
    void delete(@Param("assignmentId") Long assignmentId);
    void insertProblem(@Param("assignmentId") Long assignmentId,
                       @Param("problemId") Long problemId,
                       @Param("orderNo") int orderNo);
    Map<String, Object> getDetail(@Param("assignmentId") Long assignmentId);
    List<Map<String, Object>> getStudentProgress(@Param("assignmentId") Long assignmentId);
    List<Map<String, Object>> getListWithStats(@Param("teacherId") Long teacherId,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);

    void insertTarget(@Param("assignmentId") Long assignmentId,
                      @Param("classId") Long classId,
                      @Param("studentId") Long studentId);
}
