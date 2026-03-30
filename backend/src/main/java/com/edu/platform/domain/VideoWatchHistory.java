package com.edu.platform.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VideoWatchHistory {
    private Long id;
    private Long userId;
    private Long videoId;
    private LocalDateTime watchedAt;
    private Integer watchDurationSec;
    private String source;
}
