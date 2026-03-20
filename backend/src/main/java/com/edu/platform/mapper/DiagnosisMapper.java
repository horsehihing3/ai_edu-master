package com.edu.platform.mapper;

import com.edu.platform.domain.DiagnosisTest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface DiagnosisMapper {
    Optional<DiagnosisTest> findById(@Param("testId") Long testId);
    Optional<DiagnosisTest> findLatestByStudentId(@Param("studentId") Long studentId);
    Optional<DiagnosisTest> findInProgressByStudentId(@Param("studentId") Long studentId);
    boolean existsInProgressByStudentId(@Param("studentId") Long studentId);
    void abandonInProgressByStudentId(@Param("studentId") Long studentId);
    void insert(DiagnosisTest test);
    void update(DiagnosisTest test);
}
