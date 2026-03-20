package com.edu.platform.mapper;

import com.edu.platform.domain.WrongNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface WrongNoteMapper {
    List<WrongNote> findByStudentId(@Param("studentId") Long studentId,
                                    @Param("offset") int offset, @Param("limit") int limit);
    long countByStudentId(@Param("studentId") Long studentId);
    boolean existsByStudentAndProblem(@Param("studentId") Long studentId,
                                      @Param("problemId") Long problemId);
    Optional<WrongNote> findById(@Param("wrongNoteId") Long wrongNoteId,
                                  @Param("studentId") Long studentId);
    void insertOrUpdate(WrongNote wrongNote);
    void deleteById(@Param("wrongNoteId") Long wrongNoteId, @Param("studentId") Long studentId);
    void deleteAllByStudentId(@Param("studentId") Long studentId);
    void updateResolved(@Param("wrongNoteId") Long wrongNoteId,
                        @Param("studentId") Long studentId,
                        @Param("isResolved") boolean isResolved);
}
