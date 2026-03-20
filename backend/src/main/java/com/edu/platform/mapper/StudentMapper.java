package com.edu.platform.mapper;

import com.edu.platform.domain.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface StudentMapper {
    Optional<Student> findById(@Param("studentId") Long studentId);
    Optional<Student> findByUserId(@Param("userId") Long userId);
    List<Student> findBySchoolId(@Param("schoolId") Long schoolId,
                                  @Param("offset") int offset, @Param("limit") int limit);
    List<Student> findBySchoolIdAll(@Param("schoolId") Long schoolId);
    long countBySchoolId(@Param("schoolId") Long schoolId);
    List<Student> findBySchoolIdAndLevel(@Param("schoolId") Long schoolId,
                                          @Param("level") String level);
    void insert(Student student);
    void update(Student student);
    void updateLevel(@Param("studentId") Long studentId, @Param("level") String level);
    void updateDiagnosisAt(@Param("studentId") Long studentId);
    List<Map<String, Object>> getLevelDistribution();
    List<Map<String, Object>> getListWithStats(@Param("schoolId") Long schoolId,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);
    long countBySchoolIdForStats(@Param("schoolId") Long schoolId);
}
