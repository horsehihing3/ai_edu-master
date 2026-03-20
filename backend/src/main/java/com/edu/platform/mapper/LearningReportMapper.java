package com.edu.platform.mapper;

import com.edu.platform.domain.LearningReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface LearningReportMapper {
    Optional<LearningReport> findLatestByStudentId(@Param("studentId") Long studentId);
    List<LearningReport> findByStudentId(@Param("studentId") Long studentId,
                                          @Param("type") String type);
    void insertOrUpdate(LearningReport report);
}
