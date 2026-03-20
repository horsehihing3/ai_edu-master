package com.edu.platform.mapper;

import com.edu.platform.domain.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface VideoMapper {
    Optional<Video> findById(@Param("videoId") Long videoId);
    Optional<Video> findByProblemId(@Param("problemId") Long problemId);
    List<Video> findAll(@Param("subject") String subject, @Param("level") String level, @Param("grade") String grade,
                        @Param("schoolId") Long schoolId,
                        @Param("offset") int offset, @Param("limit") int limit);
    long countAll(@Param("subject") String subject, @Param("level") String level, @Param("grade") String grade,
                  @Param("schoolId") Long schoolId);
    List<Video> findAllForAdmin(@Param("keyword") String keyword,
                               @Param("level") String level,
                               @Param("offset") int offset, @Param("limit") int limit);
    long countAllForAdmin(@Param("keyword") String keyword, @Param("level") String level);
    List<Video> findRelated(@Param("videoId") Long videoId, @Param("level") String level,
                            @Param("unitName") String unitName, @Param("limit") int limit);
    void insert(Video video);
    void update(Video video);
    void delete(@Param("videoId") Long videoId);
    void incrementViewCount(@Param("videoId") Long videoId);
    void toggleActive(@Param("videoId") Long videoId);
}
