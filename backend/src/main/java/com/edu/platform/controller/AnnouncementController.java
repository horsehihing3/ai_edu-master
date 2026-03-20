package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Announcement;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.AnnouncementMapper;
import com.edu.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementMapper announcementMapper;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Announcement>>> getAnnouncements(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int offset = page * size;

        List<Announcement> announcements = announcementMapper.findActive(schoolId, offset, size);
        long totalElements = announcementMapper.countActive(schoolId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(announcements, totalElements, totalPages, page, size)));
    }

    @GetMapping("/{announcementId}")
    public ResponseEntity<ApiResponse<Announcement>> getAnnouncement(@PathVariable Long announcementId) {
        Announcement announcement = announcementMapper.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        announcementMapper.incrementViewCount(announcementId);
        return ResponseEntity.ok(ApiResponse.success(announcement));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> createAnnouncement(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Announcement announcement) {
        Long userId = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
        announcement.setAuthorId(userId);
        announcement.setIsActive(true);
        if (announcement.getViewCount() == null) announcement.setViewCount(0);
        announcementMapper.insert(announcement);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ANNOUNCEMENT_CREATED, announcement));
    }

    @PutMapping("/{announcementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> updateAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody Announcement request) {
        Announcement announcement = announcementMapper.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsImportant(request.getIsImportant());
        announcement.setTargetRole(request.getTargetRole());
        announcement.setStartAt(request.getStartAt());
        announcement.setEndAt(request.getEndAt());
        announcementMapper.update(announcement);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ANNOUNCEMENT_UPDATED, announcement));
    }

    @DeleteMapping("/{announcementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long announcementId) {
        announcementMapper.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        announcementMapper.delete(announcementId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ANNOUNCEMENT_DELETED));
    }
}
