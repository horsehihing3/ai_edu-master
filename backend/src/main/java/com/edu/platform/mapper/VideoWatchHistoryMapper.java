package com.edu.platform.mapper;

import com.edu.platform.domain.VideoWatchHistory;
import com.edu.platform.dto.video.VideoWatchHistoryResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VideoWatchHistoryMapper {
    void insert(VideoWatchHistory history);
    void updateWatchedAt(@Param("userId") Long userId, @Param("videoId") Long videoId,
                         @Param("watchDurationSec") Integer watchDurationSec, @Param("source") String source);
    List<VideoWatchHistoryResponseDto> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    VideoWatchHistory findByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") Long videoId);
    int countByUserId(@Param("userId") Long userId);
}
