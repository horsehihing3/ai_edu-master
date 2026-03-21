package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Student;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.dto.student.ProblemSubmitRequest;
import com.edu.platform.dto.student.SessionProgressDto;
import com.edu.platform.dto.student.StudentHomeDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.StudentMapper;
import com.edu.platform.mapper.UserMapper;
import com.edu.platform.service.FeedbackService;
import com.edu.platform.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final FeedbackService feedbackService;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<StudentHomeDto>> getHome(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        StudentHomeDto home = studentService.getStudentHome(userId);
        return ResponseEntity.ok(ApiResponse.success(home));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        return ResponseEntity.ok(ApiResponse.success(studentService.getDashboardStats(student.getStudentId())));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Object>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentHistory(student.getStudentId(), size)));
    }

    @GetMapping("/ai-comment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAiComment(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        return ResponseEntity.ok(ApiResponse.success(studentService.getAiComment(student.getStudentId())));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<Object>> getAssignments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentAssignments(student.getStudentId(), size)));
    }

    @PostMapping("/sessions/start")
    public ResponseEntity<ApiResponse<SessionProgressDto>> startSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> request) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        Long assignmentId = request.get("assignmentId");
        SessionProgressDto progress = studentService.startLearningSession(student.getStudentId(), assignmentId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SESSION_STARTED, progress));
    }

    @PostMapping("/sessions/start-single")
    public ResponseEntity<ApiResponse<SessionProgressDto>> startSingleSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> request) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        Long problemId = request.get("problemId");
        SessionProgressDto progress = studentService.startSingleSession(student.getStudentId(), problemId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SESSION_STARTED, progress));
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/sessions/start-bookmark")
    public ResponseEntity<ApiResponse<SessionProgressDto>> startBookmarkSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        List<Number> raw = (List<Number>) request.get("problemIds");
        List<Long> problemIds = raw.stream().map(Number::longValue).toList();
        SessionProgressDto progress = studentService.startBookmarkSession(student.getStudentId(), problemIds);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SESSION_STARTED, progress));
    }

    @PostMapping("/sessions/{sessionId}/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody ProblemSubmitRequest request) {
        Map<String, Object> result = studentService.submitAnswer(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ANSWER_SUBMITTED, result));
    }

    @PutMapping("/sessions/{sessionId}/save")
    public ResponseEntity<ApiResponse<Void>> saveProgress(@PathVariable Long sessionId) {
        studentService.saveSessionProgress(sessionId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SESSION_SAVED));
    }

    @GetMapping("/sessions/{sessionId}/problems")
    public ResponseEntity<ApiResponse<Object>> getSessionProblems(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getSessionProblems(sessionId)));
    }

    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<ApiResponse<SessionProgressDto>> completeSession(@PathVariable Long sessionId) {
        SessionProgressDto result = studentService.completeSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SESSION_COMPLETED, result));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getBookmarks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        PageResponse<Map<String, Object>> bookmarks = studentService.getBookmarks(student.getStudentId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }

    @PostMapping("/bookmarks")
    public ResponseEntity<ApiResponse<Void>> addBookmarkByBody(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> request) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        studentService.addBookmark(student.getStudentId(), request.get("problemId"));
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.BOOKMARK_ADDED));
    }

    @PostMapping("/bookmarks/{problemId}")
    public ResponseEntity<ApiResponse<Void>> addBookmark(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long problemId) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        studentService.addBookmark(student.getStudentId(), problemId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.BOOKMARK_ADDED));
    }

    @DeleteMapping("/bookmarks/problem/{problemId}")
    public ResponseEntity<ApiResponse<Void>> removeBookmarkByProblem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long problemId) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        studentService.removeBookmarkByProblemId(student.getStudentId(), problemId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.BOOKMARK_DELETED));
    }

    @DeleteMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookmarkId) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        studentService.removeBookmarkByProblemId(student.getStudentId(), bookmarkId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.BOOKMARK_DELETED));
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReport(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        Map<String, Object> report = studentService.getLearningReport(student.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/report/detail")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReportDetail(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        Map<String, Object> report = studentService.getLearningReport(student.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/assignments/{assignmentId}/feedback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssignmentFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long assignmentId) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getStudentFeedback(assignmentId, student.getStudentId())));
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
