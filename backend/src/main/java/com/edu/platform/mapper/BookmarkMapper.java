package com.edu.platform.mapper;

import com.edu.platform.domain.Bookmark;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface BookmarkMapper {
    Optional<Bookmark> findByStudentAndProblem(@Param("studentId") Long studentId,
                                                @Param("problemId") Long problemId);
    List<Bookmark> findByStudentId(@Param("studentId") Long studentId,
                                    @Param("offset") int offset, @Param("limit") int limit);
    long countByStudentId(@Param("studentId") Long studentId);
    boolean existsByStudentAndProblem(@Param("studentId") Long studentId,
                                       @Param("problemId") Long problemId);
    void insert(Bookmark bookmark);
    void delete(@Param("studentId") Long studentId, @Param("problemId") Long problemId);
    void updateResolved(@Param("bookmarkId") Long bookmarkId, @Param("isResolved") boolean isResolved);
}
