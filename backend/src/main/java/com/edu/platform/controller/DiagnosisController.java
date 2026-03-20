package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Student;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.student.DiagnosisStartResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.StudentMapper;
import com.edu.platform.mapper.UserMapper;
import com.edu.platform.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<DiagnosisStartResponse>> startDiagnosis(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        DiagnosisStartResponse response = diagnosisService.startDiagnosisTest(student.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.DIAGNOSIS_STARTED, response));
    }

    @PostMapping("/{testId}/submit")
    public ResponseEntity<ApiResponse<Void>> submitAnswer(
            @PathVariable Long testId,
            @RequestBody Map<String, Object> request) {
        Long problemId = Long.parseLong(request.get("problemId").toString());
        String answer = request.get("answer").toString();
        diagnosisService.submitDiagnosisAnswer(testId, problemId, answer);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.DIAGNOSIS_SUBMITTED));
    }

    @PostMapping("/{testId}/complete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completeDiagnosis(@PathVariable Long testId) {
        Map<String, Object> result = diagnosisService.completeDiagnosisTest(testId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.DIAGNOSIS_COMPLETED, result));
    }

    @GetMapping("/{testId}/result")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResult(@PathVariable Long testId) {
        Map<String, Object> result = diagnosisService.completeDiagnosisTest(testId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private Long getUserId(UserDetails userDetails) {
        return userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
    }

    private Student getStudent(Long userId) {
        return studentMapper.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
