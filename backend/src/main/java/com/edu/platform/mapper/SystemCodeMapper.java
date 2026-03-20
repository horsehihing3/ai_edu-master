package com.edu.platform.mapper;

import com.edu.platform.domain.SystemCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SystemCodeMapper {
    List<SystemCode> findAll(@Param("codeGroup") String codeGroup,
                              @Param("parentCodeId") Long parentCodeId,
                              @Param("onlyActive") boolean onlyActive);
    List<SystemCode> findRoots(@Param("codeGroup") String codeGroup,
                                @Param("onlyActive") boolean onlyActive);
    List<SystemCode> findChildren(@Param("parentCodeId") Long parentCodeId,
                                   @Param("onlyActive") boolean onlyActive);
    Optional<SystemCode> findById(@Param("codeId") Long codeId);
    Optional<SystemCode> findByGroupAndValue(@Param("codeGroup") String codeGroup,
                                              @Param("codeValue") String codeValue);
    List<String> findDistinctGroups();
    long countByGroupAndValue(@Param("codeGroup") String codeGroup,
                               @Param("codeValue") String codeValue,
                               @Param("excludeId") Long excludeId);
    void insert(SystemCode code);
    void update(SystemCode code);
    void updateActive(@Param("codeId") Long codeId, @Param("isActive") boolean isActive);
    void delete(@Param("codeId") Long codeId);
}
