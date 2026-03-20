package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Inquiry;
import com.edu.platform.domain.InquiryReply;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.InquiryMapper;
import com.edu.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryMapper inquiryMapper;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Inquiry>>> getInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(userDetails);
        int offset = page * size;
        List<Inquiry> inquiries = inquiryMapper.findByUserId(userId, offset, size);
        long totalElements = inquiryMapper.countAll(null);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(inquiries, totalElements, totalPages, page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Inquiry>> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(userDetails);
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .title(request.get("title").toString())
                .content(request.get("content").toString())
                .category(request.getOrDefault("category", "OTHER").toString())
                .isSecret(Boolean.parseBoolean(request.getOrDefault("isSecret", "false").toString()))
                .build();
        inquiryMapper.insert(inquiry);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.INQUIRY_CREATED, inquiry));
    }

    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Inquiry>> getInquiry(@PathVariable Long inquiryId) {
        Inquiry inquiry = inquiryMapper.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(inquiry));
    }

    @PostMapping("/{inquiryId}/reply")
    @PreAuthorize("hasAnyRole('TEACHER', 'SUPER_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> replyInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId,
            @RequestBody Map<String, String> request) {
        Long userId = getUserId(userDetails);
        inquiryMapper.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));

        InquiryReply reply = InquiryReply.builder()
                .inquiryId(inquiryId)
                .replierId(userId)
                .content(request.get("content"))
                .build();

        inquiryMapper.insertReply(reply);
        inquiryMapper.updateStatus(inquiryId, "RESOLVED");

        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.INQUIRY_REPLY_CREATED));
    }

    private Long getUserId(UserDetails userDetails) {
        return userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
    }
}
