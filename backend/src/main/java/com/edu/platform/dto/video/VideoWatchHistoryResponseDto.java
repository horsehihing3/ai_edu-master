package com.edu.platform.dto.video;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VideoWatchHistoryResponseDto {
    private Long id;
    private Long videoId;
    private String videoTitle;
    private LocalDateTime watchedAt;
    private String source;
}
