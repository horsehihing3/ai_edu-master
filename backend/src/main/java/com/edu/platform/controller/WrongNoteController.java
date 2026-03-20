package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Student;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.dto.student.WrongNoteDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.StudentMapper;
import com.edu.platform.mapper.UserMapper;
import com.edu.platform.service.WrongNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/wrong-notes")
@RequiredArgsConstructor
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WrongNoteDto>>> getWrongNotes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        PageResponse<WrongNoteDto> result = wrongNoteService.getWrongNotes(student.getStudentId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{wrongNoteId}")
    public ResponseEntity<ApiResponse<Void>> deleteWrongNote(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long wrongNoteId) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        wrongNoteService.deleteWrongNote(student.getStudentId(), wrongNoteId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.WRONG_NOTE_DELETED));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllWrongNotes(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        wrongNoteService.deleteAllWrongNotes(student.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.WRONG_NOTE_ALL_DELETED));
    }

    @PatchMapping("/{wrongNoteId}/resolve")
    public ResponseEntity<ApiResponse<Void>> toggleResolved(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long wrongNoteId) {
        Long userId = getUserId(userDetails);
        Student student = getStudent(userId);
        wrongNoteService.toggleResolved(student.getStudentId(), wrongNoteId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.WRONG_NOTE_RESOLVED_TOGGLED));
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
