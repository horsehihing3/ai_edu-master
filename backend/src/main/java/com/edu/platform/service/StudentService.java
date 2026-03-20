package com.edu.platform.service;

import com.edu.platform.domain.*;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.dto.student.ProblemSubmitRequest;
import com.edu.platform.dto.student.SessionProgressDto;
import com.edu.platform.dto.student.StudentHomeDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final AssignmentMapper assignmentMapper;
    private final LearningSessionMapper learningSessionMapper;
    private final BookmarkMapper bookmarkMapper;
    private final ProblemMapper problemMapper;
    private final LearningReportMapper learningReportMapper;
    private final SchoolMapper schoolMapper;

    @Transactional(readOnly = true)
    public StudentHomeDto getStudentHome(Long userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Student student = studentMapper.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        List<Assignment> assignments = assignmentMapper.findBySchoolId(student.getSchoolId());

        List<StudentHomeDto.AssignmentSummaryDto> assignmentSummaries = assignments.stream()
                .limit(5)
                .map(a -> StudentHomeDto.AssignmentSummaryDto.builder()
                        .assignmentId(a.getAssignmentId())
                        .title(a.getTitle())
                        .dueDate(a.getDueDate() != null ? a.getDueDate().toString() : null)
                        .progress(0)
                        .totalProblems(0)
                        .isCompleted(false)
                        .build())
                .collect(Collectors.toList());

        List<LearningSession> recentSessions = learningSessionMapper
                .findLatestByStudentId(student.getStudentId(), 0, 10);

        int totalSolved = recentSessions.stream().mapToInt(s -> s.getSolvedCount() != null ? s.getSolvedCount() : 0).sum();
        int totalCorrect = recentSessions.stream().mapToInt(s -> s.getCorrectCount() != null ? s.getCorrectCount() : 0).sum();
        double accuracy = totalSolved > 0 ? (double) totalCorrect / totalSolved * 100 : 0;

        StudentHomeDto.RecentProgressDto recentProgress = StudentHomeDto.RecentProgressDto.builder()
                .totalSolved(totalSolved)
                .correctCount(totalCorrect)
                .accuracy(accuracy)
                .studyTimeSec(0)
                .build();

        return StudentHomeDto.builder()
                .userName(user.getName())
                .studentLevel(student.getStudentLevelAsEnum())
                .currentAssignments(assignmentSummaries)
                .recentProgress(recentProgress)
                .totalSolvedCount(totalSolved)
                .build();
    }

    @Transactional
    public SessionProgressDto startLearningSession(Long studentId, Long assignmentId) {
        studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        assignmentMapper.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        int totalProblems = problemMapper.findByAssignmentId(assignmentId).size();

        LearningSession session = LearningSession.builder()
                .studentId(studentId)
                .assignmentId(assignmentId)
                .sessionType(LearningSession.SessionType.ASSIGNMENT.name())
                .status(LearningSession.SessionStatus.IN_PROGRESS.name())
                .totalProblems(totalProblems)
                .solvedCount(0)
                .correctCount(0)
                .startedAt(LocalDateTime.now())
                .build();

        learningSessionMapper.insert(session);

        return SessionProgressDto.builder()
                .sessionId(session.getSessionId())
                .solvedCount(0)
                .totalProblems(totalProblems)
                .correctCount(0)
                .completionRate(0.0)
                .accuracy(0.0)
                .status(session.getStatus())
                .build();
    }

    @Transactional
    public SessionProgressDto startBookmarkSession(Long studentId, List<Long> problemIdList) {
        studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        String problemIdsStr = problemIdList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        LearningSession session = LearningSession.builder()
                .studentId(studentId)
                .sessionType(LearningSession.SessionType.BOOKMARK.name())
                .problemIds(problemIdsStr)
                .status(LearningSession.SessionStatus.IN_PROGRESS.name())
                .totalProblems(problemIdList.size())
                .solvedCount(0)
                .correctCount(0)
                .startedAt(LocalDateTime.now())
                .build();

        learningSessionMapper.insert(session);

        return SessionProgressDto.builder()
                .sessionId(session.getSessionId())
                .solvedCount(0)
                .totalProblems(problemIdList.size())
                .correctCount(0)
                .completionRate(0.0)
                .accuracy(0.0)
                .status(session.getStatus())
                .build();
    }

    @Transactional
    public Map<String, Object> submitAnswer(Long sessionId, ProblemSubmitRequest request) {
        LearningSession session = learningSessionMapper.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.SESSION_NOT_IN_PROGRESS);
        }

        Problem problem = problemMapper.findById(request.getProblemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        boolean isCorrect = problem.getAnswer() != null &&
                problem.getAnswer().equalsIgnoreCase(request.getSubmittedAnswer());

        ProblemAttempt attempt = ProblemAttempt.builder()
                .sessionId(sessionId)
                .studentId(session.getStudentId())
                .problemId(request.getProblemId())
                .submittedAnswer(request.getSubmittedAnswer())
                .isCorrect(isCorrect)
                .timeSpentSec(request.getTimeSpentSec() != null ? request.getTimeSpentSec() : 0)
                .isBookmarked(false)
                .requestedVideo(false)
                .feedbackLike(null)
                .attemptedAt(LocalDateTime.now())
                .build();

        learningSessionMapper.insertAttempt(attempt);

        session.setSolvedCount(session.getSolvedCount() != null ? session.getSolvedCount() + 1 : 1);
        if (isCorrect) {
            session.setCorrectCount(session.getCorrectCount() != null ? session.getCorrectCount() + 1 : 1);
        }
        learningSessionMapper.update(session);

        return Map.of(
                "isCorrect", isCorrect,
                "correctAnswer", problem.getAnswer() != null ? problem.getAnswer() : "",
                "explanation", problem.getExplanation() != null ? problem.getExplanation() : "",
                "hint", problem.getHint() != null ? problem.getHint() : ""
        );
    }

    @Transactional
    public void saveSessionProgress(Long sessionId) {
        LearningSession session = learningSessionMapper.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        session.setLastSavedAt(LocalDateTime.now());
        session.setStatus(LearningSession.SessionStatus.PAUSED.name());
        learningSessionMapper.update(session);
    }

    @Transactional
    public SessionProgressDto completeSession(Long sessionId) {
        LearningSession session = learningSessionMapper.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        session.setStatus(LearningSession.SessionStatus.COMPLETED.name());
        session.setCompletedAt(LocalDateTime.now());

        int totalProblems = session.getTotalProblems() != null ? session.getTotalProblems() : 0;
        if (totalProblems == 0 && session.getAssignmentId() != null) {
            totalProblems = problemMapper.findByAssignmentId(session.getAssignmentId()).size();
            session.setTotalProblems(totalProblems);
        }
        int solvedCount = session.getSolvedCount() != null ? session.getSolvedCount() : 0;
        int correctCount = session.getCorrectCount() != null ? session.getCorrectCount() : 0;

        if (totalProblems > 0) {
            session.setCompletionRateFromDouble((double) solvedCount / totalProblems * 100);
        }

        learningSessionMapper.update(session);

        double accuracy = solvedCount > 0 ? (double) correctCount / solvedCount * 100 : 0;

        return SessionProgressDto.builder()
                .sessionId(session.getSessionId())
                .solvedCount(solvedCount)
                .totalProblems(totalProblems)
                .correctCount(correctCount)
                .completionRate(session.getCompletionRateAsDouble())
                .accuracy(accuracy)
                .status(session.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getBookmarks(Long studentId, int page, int size) {
        int offset = page * size;

        List<Bookmark> bookmarks = bookmarkMapper.findByStudentId(studentId, offset, size);
        long totalElements = bookmarkMapper.countByStudentId(studentId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<Map<String, Object>> content = bookmarks.stream()
                .map(b -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("bookmarkId", b.getBookmarkId());
                    entry.put("problemId", b.getProblemId());
                    entry.put("memo", b.getMemo() != null ? b.getMemo() : "");
                    entry.put("isResolved", b.getIsResolved() != null ? b.getIsResolved() : false);
                    entry.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
                    problemMapper.findById(b.getProblemId()).ifPresent(p -> {
                        entry.put("subject", p.getSubject() != null ? p.getSubject() : "");
                        entry.put("level", p.getLevel() != null ? p.getLevel() : "");
                        entry.put("unitName", p.getUnitName() != null ? p.getUnitName() : "");
                        entry.put("questionText", p.getQuestionText() != null ? p.getQuestionText() : "");
                    });
                    return entry;
                })
                .collect(Collectors.toList());

        return PageResponse.of(content, totalElements, totalPages, page, size);
    }

    @Transactional
    public void toggleBookmark(Long studentId, Long problemId) {
        if (bookmarkMapper.existsByStudentAndProblem(studentId, problemId)) {
            bookmarkMapper.delete(studentId, problemId);
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .studentId(studentId)
                    .problemId(problemId)
                    .build();
            bookmarkMapper.insert(bookmark);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(Long studentId) {
        Student student = studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        List<LearningSession> sessions = learningSessionMapper.findAllByStudentId(studentId);
        int totalSolved = sessions.stream().mapToInt(s -> s.getSolvedCount() != null ? s.getSolvedCount() : 0).sum();
        int totalCorrect = sessions.stream().mapToInt(s -> s.getCorrectCount() != null ? s.getCorrectCount() : 0).sum();
        double accuracy = totalSolved > 0 ? Math.round((double) totalCorrect / totalSolved * 100.0) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalSolved", totalSolved);
        result.put("accuracy", accuracy);
        result.put("streak", 0);
        result.put("todaySolved", 0);
        result.put("todayTime", 0);
        result.put("todayAccuracy", accuracy);
        result.put("level", student.getStudentLevel() != null ? student.getStudentLevel() : "N/A");
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentHistory(Long studentId, int size) {
        List<LearningSession> sessions = learningSessionMapper.findLatestByStudentId(studentId, 0, size);
        return sessions.stream().map(s -> {
            int solved = s.getSolvedCount() != null ? s.getSolvedCount() : 0;
            int correct = s.getCorrectCount() != null ? s.getCorrectCount() : 0;
            double acc = solved > 0 ? Math.round((double) correct / solved * 100.0) : 0;
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", s.getStartedAt() != null ? s.getStartedAt().toLocalDate().toString() : "");
            entry.put("subject", "종합");
            entry.put("problemCount", solved);
            entry.put("accuracy", (int) acc);
            entry.put("sessionId", s.getSessionId());
            return entry;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAiComment(Long studentId) {
        List<LearningSession> sessions = learningSessionMapper.findLatestByStudentId(studentId, 0, 5);
        int totalSolved = sessions.stream().mapToInt(s -> s.getSolvedCount() != null ? s.getSolvedCount() : 0).sum();
        int totalCorrect = sessions.stream().mapToInt(s -> s.getCorrectCount() != null ? s.getCorrectCount() : 0).sum();
        double accuracy = totalSolved > 0 ? (double) totalCorrect / totalSolved * 100 : 0;

        String comment;
        if (totalSolved == 0) {
            comment = "아직 학습 기록이 없습니다. 오늘 첫 번째 문제를 풀어보세요!";
        } else if (accuracy >= 80) {
            comment = String.format("최근 정답률 %.0f%%로 훌륭한 성과를 보이고 있습니다. 이 기세를 유지해보세요!", accuracy);
        } else if (accuracy >= 60) {
            comment = String.format("정답률 %.0f%%입니다. 틀린 문제를 복습하면 빠르게 실력이 향상될 거예요.", accuracy);
        } else {
            comment = "기초 개념을 다시 한 번 점검해보세요. 꾸준한 학습이 실력을 만듭니다.";
        }
        Map<String, Object> result = new HashMap<>();
        result.put("comment", comment);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSessionProblems(Long sessionId) {
        LearningSession session = learningSessionMapper.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        List<Problem> problems = new ArrayList<>();
        if ("BOOKMARK".equals(session.getSessionType()) && session.getProblemIds() != null) {
            for (String idStr : session.getProblemIds().split(",")) {
                Long pid = Long.parseLong(idStr.trim());
                problemMapper.findById(pid).ifPresent(problems::add);
            }
        } else if (session.getAssignmentId() != null) {
            problems = problemMapper.findByAssignmentId(session.getAssignmentId());
        }

        return problems.stream().map(p -> {
            List<ProblemOption> options = problemMapper.findOptionsByProblemId(p.getProblemId());
            Map<String, Object> entry = new HashMap<>();
            entry.put("problemId", p.getProblemId());
            entry.put("subject", p.getUnitName() != null ? p.getUnitName() : "");
            entry.put("level", p.getLevel() != null ? p.getLevel() : "");
            entry.put("unitName", p.getUnitName() != null ? p.getUnitName() : "");
            entry.put("questionText", p.getQuestionText());
            entry.put("explanation", p.getExplanation() != null ? p.getExplanation() : "");
            entry.put("options", options.stream().map(o -> {
                Map<String, Object> opt = new HashMap<>();
                opt.put("optionId", o.getOptionId());
                opt.put("content", o.getOptionText());
                opt.put("isCorrect", p.getAnswer() != null && p.getAnswer().equals(String.valueOf(o.getOptionNo())));
                return opt;
            }).collect(Collectors.toList()));
            return entry;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentAssignments(Long studentId, int size) {
        Student student = studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        List<Assignment> assignments = assignmentMapper.findByStudentId(studentId);
        return assignments.stream().limit(size).map(a -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("assignmentId", a.getAssignmentId());
            entry.put("title", a.getTitle());
            entry.put("description", a.getDescription());
            entry.put("level", a.getTargetLevel());
            entry.put("dueDate", a.getDueDate() != null ? a.getDueDate().toString() : null);
            entry.put("totalCount", problemMapper.findByAssignmentId(a.getAssignmentId()).size());

            Optional<LearningSession> latestSession = learningSessionMapper
                    .findLatestByStudentAndAssignment(studentId, a.getAssignmentId());
            if (latestSession.isPresent()) {
                LearningSession s = latestSession.get();
                int total = s.getTotalProblems() != null ? s.getTotalProblems() : 0;
                int solved = s.getSolvedCount() != null ? s.getSolvedCount() : 0;
                int progress = total > 0 ? Math.round((float) solved / total * 100) : 0;
                entry.put("progress", progress);
                entry.put("sessionId", s.getSessionId());
                if ("COMPLETED".equals(s.getStatus())) {
                    entry.put("status", "done");
                } else if ("IN_PROGRESS".equals(s.getStatus())) {
                    entry.put("status", "in_progress");
                } else {
                    entry.put("status", "pending");
                }
            } else {
                entry.put("progress", 0);
                entry.put("status", "pending");
            }
            return entry;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addBookmark(Long studentId, Long problemId) {
        if (!bookmarkMapper.existsByStudentAndProblem(studentId, problemId)) {
            Bookmark bookmark = Bookmark.builder()
                    .studentId(studentId)
                    .problemId(problemId)
                    .build();
            bookmarkMapper.insert(bookmark);
        }
    }

    @Transactional
    public void removeBookmarkByProblemId(Long studentId, Long problemId) {
        bookmarkMapper.delete(studentId, problemId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLearningReport(Long studentId) {
        Student student = studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        List<LearningSession> sessions = learningSessionMapper.findAllByStudentId(studentId);
        int totalSolved = sessions.stream().mapToInt(s -> s.getSolvedCount() != null ? s.getSolvedCount() : 0).sum();
        int totalCorrect = sessions.stream().mapToInt(s -> s.getCorrectCount() != null ? s.getCorrectCount() : 0).sum();
        double accuracy = totalSolved > 0 ? (double) totalCorrect / totalSolved * 100 : 0;

        return Map.of(
                "studentId", studentId,
                "studentLevel", student.getStudentLevel() != null ? student.getStudentLevel() : "N/A",
                "totalSolved", totalSolved,
                "totalCorrect", totalCorrect,
                "accuracy", accuracy,
                "completedSessions", sessions.stream()
                        .filter(s -> "COMPLETED".equals(s.getStatus())).count()
        );
    }
}
