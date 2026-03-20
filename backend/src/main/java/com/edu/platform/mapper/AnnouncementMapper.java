package com.edu.platform.mapper;

import com.edu.platform.domain.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AnnouncementMapper {
    Optional<Announcement> findById(@Param("announcementId") Long announcementId);
    List<Announcement> findActive(@Param("schoolId") Long schoolId,
                                   @Param("offset") int offset, @Param("limit") int limit);
    long countActive(@Param("schoolId") Long schoolId);
    void insert(Announcement announcement);
    void update(Announcement announcement);
    void delete(@Param("announcementId") Long announcementId);
    void incrementViewCount(@Param("announcementId") Long announcementId);
}
