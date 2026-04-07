package com.edu.platform.controller;

import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * [2026-04-07] DRM — 콘텐츠 보안 강화
 * S3 이미지에 대한 Presigned URL을 발급하는 엔드포인트.
 * 인증된 사용자만 접근 가능하며, 발급된 URL은 15분간 유효.
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final S3Service s3Service;

    /**
     * S3 이미지 URL → 15분짜리 Presigned URL 반환
     * 프론트엔드에서 문제 이미지 표시 시 사용.
     */
    @GetMapping("/presign")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, String>> presignUrl(
            @RequestParam("url") String url) {
        String presignedUrl = s3Service.generatePresignedUrl(url, 15);
        return ApiResponse.success(Map.of("url", presignedUrl));
    }
}
