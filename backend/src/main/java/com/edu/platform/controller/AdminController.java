package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Payment;
import com.edu.platform.domain.School;
import com.edu.platform.domain.Video;
import com.edu.platform.dto.admin.AdminDashboardDto;
import com.edu.platform.dto.admin.RejectProblemRequest;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.PaymentMapper;
import com.edu.platform.mapper.SchoolMapper;
import com.edu.platform.mapper.UserMapper;
import com.edu.platform.mapper.VideoMapper;
import com.edu.platform.service.AdminService;
import com.edu.platform.service.ProblemService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SchoolMapper schoolMapper;
    private final ProblemService problemService;
    private final VideoMapper videoMapper;
    private final PaymentMapper paymentMapper;
    private final UserMapper userMapper;

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard() {
        AdminDashboardDto dashboard = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> params = Map.of(
                "role", role != null ? role : "",
                "keyword", search != null ? search : ""
        );
        PageResponse<Map<String, Object>> users = adminService.getUserList(params, page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request) {
        adminService.updateUserStatus(userId, request.get("isActive"));
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_STATUS_UPDATED));
    }

    @PostMapping("/users/{userId}/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(@PathVariable Long userId) {
        adminService.resetUserPassword(userId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_PASSWORD_RESET));
    }

    @GetMapping("/schools")
    public ResponseEntity<ApiResponse<List<School>>> getSchools() {
        List<School> schools = schoolMapper.findAll();
        return ResponseEntity.ok(ApiResponse.success(schools));
    }

    @PostMapping("/schools")
    public ResponseEntity<ApiResponse<School>> createSchool(@RequestBody School school) {
        if (schoolMapper.findByCode(school.getSchoolCode()).isPresent()) {
            throw new BusinessException(ErrorCode.SCHOOL_ALREADY_EXISTS);
        }
        schoolMapper.insert(school);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SCHOOL_CREATED, school));
    }

    @PutMapping("/schools/{schoolId}")
    public ResponseEntity<ApiResponse<School>> updateSchool(
            @PathVariable Long schoolId,
            @RequestBody School school) {
        School existing = schoolMapper.findById(schoolId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOOL_NOT_FOUND));
        existing.setSchoolName(school.getSchoolName());
        existing.setAddress(school.getAddress());
        existing.setPhone(school.getPhone());
        schoolMapper.update(existing);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SCHOOL_UPDATED, existing));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PageResponse<Payment>>> getPayments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        int offset = page * size;
        List<Payment> list = paymentMapper.findAll(
                keyword != null ? keyword : "",
                status != null ? status : "",
                offset, size);
        long total = paymentMapper.countAll(
                keyword != null ? keyword : "",
                status != null ? status : "");
        int totalPages = (int) Math.ceil((double) total / size);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(list, total, totalPages, page, size)));
    }

    @GetMapping("/payments/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStats() {
        Map<String, Object> stats = paymentMapper.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        Map<String, Object> analytics = adminService.getSystemAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    @GetMapping("/dashboard/dau")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDashboardDau() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDauData(7)));
    }

    @GetMapping("/dashboard/level-dist")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDashboardLevelDist() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getLevelDistribution()));
    }

    @GetMapping("/inquiries")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminInquiries(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAdminInquiries(status, page, size)));
    }

    @GetMapping("/inquiries/{inquiryId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInquiryDetail(@PathVariable Long inquiryId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getInquiryDetail(inquiryId)));
    }

    @PostMapping("/inquiries/{inquiryId}/reply")
    public ResponseEntity<ApiResponse<Void>> replyToInquiry(
            @PathVariable Long inquiryId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminUserId = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
        adminService.replyToInquiry(inquiryId, adminUserId, request.get("content"));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/analytics/dau")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAnalyticsDau() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDauData(30)));
    }

    @GetMapping("/analytics/level-dist")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAnalyticsLevelDist() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getLevelDistribution()));
    }

    @GetMapping("/analytics/weak-units")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAnalyticsWeakUnits() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getWeakUnitStats()));
    }

    // ─── 문제 관리 ────────────────────────────────────────────────────────────────

    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getProblems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Map<String, Object>> result = adminService.getAdminProblems(
                approvalStatus != null ? approvalStatus : "",
                level != null ? level : "",
                keyword != null ? keyword : "",
                page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProblem(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminUserId = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
        Long problemId = problemService.createProblem(request, adminUserId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROBLEM_CREATED, Map.of("problemId", problemId)));
    }

    @PutMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> updateProblem(
            @PathVariable Long problemId,
            @RequestBody Map<String, Object> request) {
        problemService.updateProblem(problemId, request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROBLEM_UPDATED));
    }

    @DeleteMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Long problemId) {
        problemService.deleteProblem(problemId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROBLEM_DELETED));
    }

    @PatchMapping("/problems/{problemId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveProblem(
            @PathVariable Long problemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminUserId = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
        adminService.approveProblem(problemId, adminUserId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROBLEM_APPROVED));
    }

    @PatchMapping("/problems/{problemId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectProblem(
            @PathVariable Long problemId,
            @Valid @RequestBody RejectProblemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminUserId = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
        adminService.rejectProblem(problemId, adminUserId, request.getRejectReason());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROBLEM_REJECTED));
    }

    // ─── 동영상 관리 ──────────────────────────────────────────────────────────────

    @PostMapping(value = "/videos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadVideoFile(
            @RequestParam("file") MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String fileName = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadPath, "videos").toAbsolutePath();
        Files.createDirectories(dir);
        Files.write(dir.resolve(fileName), file.getBytes());
        String fileUrl = "/api/uploads/videos/" + fileName;
        return ResponseEntity.ok(ApiResponse.success(Map.of("fileUrl", fileUrl)));
    }

    @GetMapping("/videos")
    public ResponseEntity<ApiResponse<PageResponse<Video>>> getVideos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String kw = keyword != null ? keyword : "";
        String lv = level != null ? level : "";
        List<Video> videos = videoMapper.findAllForAdmin(kw, lv, page * size, size);
        long total = videoMapper.countAllForAdmin(kw, lv);
        int totalPages = (int) Math.ceil((double) total / size);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(videos, total, totalPages, page, size)));
    }

    @PostMapping("/videos")
    public ResponseEntity<ApiResponse<Video>> createVideo(@RequestBody Video video) {
        videoMapper.insert(video);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.VIDEO_UPLOADED, video));
    }

    @PutMapping("/videos/{videoId}")
    public ResponseEntity<ApiResponse<Video>> updateVideo(@PathVariable Long videoId, @RequestBody Video video) {
        videoMapper.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
        video.setVideoId(videoId);
        videoMapper.update(video);
        return ResponseEntity.ok(ApiResponse.success(video));
    }

    @PutMapping("/videos/{videoId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleVideo(@PathVariable Long videoId) {
        videoMapper.toggleActive(videoId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/videos/{videoId}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable Long videoId) {
        videoMapper.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
        videoMapper.delete(videoId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.VIDEO_DELETED));
    }
}
