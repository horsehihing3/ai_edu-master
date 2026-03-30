package com.edu.platform.dto.video;

import lombok.Data;

@Data
public class VideoWatchHistoryRequestDto {
    private Integer watchDurationSec;
    private String source = "DIRECT";
}
