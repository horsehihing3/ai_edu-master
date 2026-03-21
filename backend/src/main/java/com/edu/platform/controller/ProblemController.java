package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.UserMapper;
import com.edu.platform.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> searchProblems(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String unitName,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("level", level != null ? level : "");
        params.put("grade", grade != null ? grade : "");
        params.put("unitName", unitName != null ? unitName : "");
        params.put("keyword", keyword != null ? keyword : "");
        PageResponse<Map<String, Object>> problems = problemService.searchProblems(params, page, size);
        return ResponseEntity.ok(ApiResponse.success(problems));
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProblemDetail(@PathVariable Long problemId) {
        Map<String, Object> problem = problemService.getProblemDetail(problemId);
        return ResponseEntity.ok(ApiResponse.success(problem));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'SUPER_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProblem(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
        Long problemId = problemService.createProblem(request, userId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROBLEM_CREATED, Map.of("problemId", problemId)));
    }

    @PutMapping("/{problemId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'SUPER_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateProblem(
            @PathVariable Long problemId,
            @RequestBody Map<String, Object> request) {
        problemService.updateProblem(problemId, request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROBLEM_UPDATED));
    }

    @DeleteMapping("/{problemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Long problemId) {
        problemService.deleteProblem(problemId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROBLEM_DELETED));
    }

    @PostMapping("/upload/batch")
    @PreAuthorize("hasAnyRole('TEACHER', 'SUPER_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadBatch(
            @RequestBody List<Map<String, Object>> problems,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
        Map<String, Object> result = problemService.uploadBatch(problems, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/upload/batch/{batchId}/status")
    @PreAuthorize("hasAnyRole('TEACHER', 'SUPER_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBatchStatus(@PathVariable String batchId) {
        // 배치 업로드는 동기 처리이므로 항상 완료 상태 반환
        return ResponseEntity.ok(ApiResponse.success(Map.of("batchId", batchId, "status", "COMPLETED")));
    }
}
