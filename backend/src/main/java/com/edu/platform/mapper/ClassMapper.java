package com.edu.platform.mapper;

import com.edu.platform.domain.SchoolClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface ClassMapper {
    List<SchoolClass> findByTeacherId(@Param("teacherId") Long teacherId);
    Optional<SchoolClass> findById(@Param("classId") Long classId);
    int countMembers(@Param("classId") Long classId);
    void insert(SchoolClass schoolClass);
    void update(SchoolClass schoolClass);
    void deleteById(@Param("classId") Long classId);

    List<Map<String, Object>> findMembersByClassId(@Param("classId") Long classId);
    void addMember(@Param("classId") Long classId, @Param("studentId") Long studentId);
    void removeMember(@Param("classId") Long classId, @Param("studentId") Long studentId);
    int countMemberByStudentId(@Param("classId") Long classId, @Param("studentId") Long studentId);
}
