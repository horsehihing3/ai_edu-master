package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Video;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoMapper videoMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Video>>> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Long schoolId) {
        int offset = page * size;
        List<Video> videos = videoMapper.findAll(subject, level, grade, schoolId, offset, size);
        long totalElements = videoMapper.countAll(subject, level, grade, schoolId);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(videos, totalElements, totalPages, page, size)));
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<ApiResponse<Video>> getVideo(@PathVariable Long videoId) {
        Video video = videoMapper.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
        videoMapper.incrementViewCount(videoId);
        return ResponseEntity.ok(ApiResponse.success(video));
    }

    @GetMapping("/{videoId}/related")
    public ResponseEntity<ApiResponse<List<Video>>> getRelatedVideos(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "5") int limit) {
        Video video = videoMapper.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
        List<Video> related = videoMapper.findRelated(videoId, video.getLevel(), video.getUnitName(), limit);
        return ResponseEntity.ok(ApiResponse.success(related));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestVideo(@RequestBody Map<String, Object> request) {
        // TODO: 동영상 풀이 요청 처리
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.VIDEO_REQUEST_ACCEPTED));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Video>> uploadVideo(@RequestBody Video video) {
        videoMapper.insert(video);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.VIDEO_UPLOADED, video));
    }

    @DeleteMapping("/{videoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable Long videoId) {
        videoMapper.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
        videoMapper.delete(videoId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.VIDEO_DELETED));
    }
}
