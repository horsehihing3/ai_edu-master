package com.edu.platform.mapper;

import com.edu.platform.domain.School;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SchoolMapper {
    Optional<School> findById(@Param("schoolId") Long schoolId);
    Optional<School> findByCode(@Param("schoolCode") String schoolCode);
    List<School> findAll();
    void insert(School school);
    void update(School school);
    long count();
}
