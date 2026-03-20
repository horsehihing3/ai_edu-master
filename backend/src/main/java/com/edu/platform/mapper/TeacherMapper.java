package com.edu.platform.mapper;

import com.edu.platform.domain.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface TeacherMapper {
    Optional<Teacher> findById(@Param("teacherId") Long teacherId);
    Optional<Teacher> findByUserId(@Param("userId") Long userId);
    void insert(Teacher teacher);
    void update(Teacher teacher);
    List<Map<String, Object>> getSubjectStats(@Param("schoolId") Long schoolId);
    List<Map<String, Object>> getWeeklyParticipation(@Param("schoolId") Long schoolId);
    List<Map<String, Object>> getStudentStats(@Param("schoolId") Long schoolId);
}
