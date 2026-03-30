package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.User;
import com.edu.platform.domain.Video;
import com.edu.platform.domain.VideoWatchHistory;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.dto.video.VideoWatchHistoryRequestDto;
import com.edu.platform.dto.video.VideoWatchHistoryResponseDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.UserMapper;
import com.edu.platform.mapper.VideoMapper;
import com.edu.platform.mapper.VideoWatchHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoMapper videoMapper;
    private final VideoWatchHistoryMapper videoWatchHistoryMapper;
    private final UserMapper userMapper;

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

    // [2026-03-30] 문제별 연결 영상 조회 — /{videoId} 보다 앞에 선언해야 경로 충돌 방지
    @GetMapping("/by-problem/{problemId}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getVideoByProblem(
            @PathVariable Long problemId) {
        Video video = videoMapper.findByProblemId(problemId).orElse(null);
        if (video == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.success(video));
    }

    // [2026-03-30] 시청 이력 조회 — /{videoId} 보다 앞에 선언해야 경로 충돌 방지
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<ApiResponse<List<VideoWatchHistoryResponseDto>>> getWatchHistory(
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<VideoWatchHistoryResponseDto> history = videoWatchHistoryMapper.findByUserId(user.getUserId(), limit);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<ApiResponse<Video>> getVideo(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Video video = videoMapper.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
        videoMapper.incrementViewCount(videoId);

        // [2026-03-30] 로그인 사용자의 경우 DIRECT 시청 이력 저장
        if (userDetails != null) {
            userMapper.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                VideoWatchHistory existing = videoWatchHistoryMapper.findByUserIdAndVideoId(user.getUserId(), videoId);
                if (existing == null) {
                    VideoWatchHistory history = new VideoWatchHistory();
                    history.setUserId(user.getUserId());
                    history.setVideoId(videoId);
                    history.setSource("DIRECT");
                    videoWatchHistoryMapper.insert(history);
                } else {
                    videoWatchHistoryMapper.updateWatchedAt(user.getUserId(), videoId, existing.getWatchDurationSec(), "DIRECT");
                }
            });
        }

        return ResponseEntity.ok(ApiResponse.success(video));
    }

    // [2026-03-30] 시청 이력 수동 저장 (source, watchDurationSec 지정 가능)
    @PostMapping("/{videoId}/watch")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> recordWatch(
            @PathVariable Long videoId,
            @RequestBody VideoWatchHistoryRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        videoMapper.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
        User user = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String source = request.getSource() != null ? request.getSource() : "DIRECT";
        VideoWatchHistory existing = videoWatchHistoryMapper.findByUserIdAndVideoId(user.getUserId(), videoId);
        if (existing == null) {
            VideoWatchHistory history = new VideoWatchHistory();
            history.setUserId(user.getUserId());
            history.setVideoId(videoId);
            history.setWatchDurationSec(request.getWatchDurationSec());
            history.setSource(source);
            videoWatchHistoryMapper.insert(history);
        } else {
            videoWatchHistoryMapper.updateWatchedAt(user.getUserId(), videoId, request.getWatchDurationSec(), source);
        }
        return ResponseEntity.ok(ApiResponse.success(null));
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
