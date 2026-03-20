package com.edu.platform.service;

import com.edu.platform.domain.AssignmentFeedback;
import com.edu.platform.domain.AttemptFeedback;
import com.edu.platform.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;

    // 학생별 문제 풀이 상세 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentAttemptDetails(Long assignmentId, Long studentId) {
        return feedbackMapper.getStudentAttemptDetails(assignmentId, studentId);
    }

    // 과제 전체 피드백 조회
    @Transactional(readOnly = true)
    public AssignmentFeedback getAssignmentFeedback(Long assignmentId, Long studentId) {
        return feedbackMapper.findAssignmentFeedback(assignmentId, studentId).orElse(null);
    }

    // 과제 전체 피드백 저장 (insert or update)
    @Transactional
    public AssignmentFeedback saveAssignmentFeedback(Long assignmentId, Long studentId, Long teacherId,
                                                      String comment, String drawingUrl) {
        Optional<AssignmentFeedback> existing = feedbackMapper.findAssignmentFeedback(assignmentId, studentId);
        if (existing.isPresent()) {
            AssignmentFeedback fb = existing.get();
            fb.setComment(comment);
            fb.setDrawingUrl(drawingUrl);
            feedbackMapper.updateAssignmentFeedback(fb);
            return fb;
        } else {
            AssignmentFeedback fb = AssignmentFeedback.builder()
                    .assignmentId(assignmentId)
                    .studentId(studentId)
                    .teacherId(teacherId)
                    .comment(comment)
                    .drawingUrl(drawingUrl)
                    .build();
            feedbackMapper.insertAssignmentFeedback(fb);
            return fb;
        }
    }

    // 문제별 피드백 저장 (insert or update)
    @Transactional
    public AttemptFeedback saveAttemptFeedback(Long attemptId, Long teacherId,
                                                String comment, String drawingUrl) {
        Optional<AttemptFeedback> existing = feedbackMapper.findAttemptFeedback(attemptId);
        if (existing.isPresent()) {
            AttemptFeedback fb = existing.get();
            fb.setComment(comment);
            fb.setDrawingUrl(drawingUrl);
            feedbackMapper.updateAttemptFeedback(fb);
            return fb;
        } else {
            AttemptFeedback fb = AttemptFeedback.builder()
                    .attemptId(attemptId)
                    .teacherId(teacherId)
                    .comment(comment)
                    .drawingUrl(drawingUrl)
                    .build();
            feedbackMapper.insertAttemptFeedback(fb);
            return fb;
        }
    }

    // 학생용: 과제 피드백 조회 (전체 + 문제별)
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentFeedback(Long assignmentId, Long studentId) {
        AssignmentFeedback overall = feedbackMapper.findAssignmentFeedback(assignmentId, studentId).orElse(null);
        List<AttemptFeedback> attemptFeedbacks = feedbackMapper.findAttemptFeedbacksByAssignment(assignmentId, studentId);
        List<Map<String, Object>> attemptDetails = feedbackMapper.getStudentAttemptDetails(assignmentId, studentId);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("overallFeedback", overall);
        result.put("attemptFeedbacks", attemptFeedbacks);
        result.put("attemptDetails", attemptDetails);
        return result;
    }
}
