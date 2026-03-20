package com.edu.platform.service;

import com.edu.platform.domain.DiagnosisTest;
import com.edu.platform.domain.Problem;
import com.edu.platform.domain.ProblemAttempt;
import com.edu.platform.domain.Student;
import com.edu.platform.dto.student.DiagnosisStartResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.DiagnosisMapper;
import com.edu.platform.mapper.LearningSessionMapper;
import com.edu.platform.mapper.ProblemMapper;
import com.edu.platform.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private static final int DIAGNOSIS_PROBLEM_COUNT = 20;

    private final DiagnosisMapper diagnosisMapper;
    private final StudentMapper studentMapper;
    private final ProblemMapper problemMapper;
    private final LearningSessionMapper learningSessionMapper;

    @Transactional
    public DiagnosisStartResponse startDiagnosisTest(Long studentId) {
        // 이미 진행 중인 진단 테스트가 있으면 ABANDONED 처리 후 새로 시작
        diagnosisMapper.abandonInProgressByStudentId(studentId);

        DiagnosisTest test = DiagnosisTest.builder()
                .studentId(studentId)
                .status(DiagnosisTest.DiagnosisStatus.IN_PROGRESS.name())
                .startedAt(LocalDateTime.now())
                .totalQuestions(DIAGNOSIS_PROBLEM_COUNT)
                .build();

        diagnosisMapper.insert(test);

        // 레벨 B 기준으로 진단 문제 랜덤 선택
        List<Problem> problems = problemMapper.findForDiagnosis(
                Problem.Level.B.name(), DIAGNOSIS_PROBLEM_COUNT);

        List<DiagnosisStartResponse.ProblemDto> problemDtos = problems.stream()
                .map(this::mapToProblemDto)
                .collect(Collectors.toList());

        return DiagnosisStartResponse.builder()
                .testId(test.getTestId())
                .problems(problemDtos)
                .totalCount(problemDtos.size())
                .build();
    }

    @Transactional
    public void submitDiagnosisAnswer(Long testId, Long problemId, String answer) {
        DiagnosisTest test = diagnosisMapper.findById(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        if (!"IN_PROGRESS".equals(test.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "진행 중인 진단 테스트가 아닙니다.");
        }

        Problem problem = problemMapper.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        boolean isCorrect = problem.getAnswer() != null &&
                problem.getAnswer().equalsIgnoreCase(answer);

        ProblemAttempt attempt = ProblemAttempt.builder()
                .sessionId(testId)
                .studentId(test.getStudentId())
                .problemId(problemId)
                .submittedAnswer(answer)
                .isCorrect(isCorrect)
                .attemptedAt(LocalDateTime.now())
                .build();

        learningSessionMapper.insertAttempt(attempt);
    }

    @Transactional
    public Map<String, Object> completeDiagnosisTest(Long testId) {
        DiagnosisTest test = diagnosisMapper.findById(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        if (!"IN_PROGRESS".equals(test.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "진행 중인 진단 테스트가 아닙니다.");
        }

        long correctCount = learningSessionMapper.countCorrectAttemptsByStudentId(test.getStudentId(), true);
        int total = test.getTotalQuestions() != null ? test.getTotalQuestions() : DIAGNOSIS_PROBLEM_COUNT;
        double scoreRate = total > 0 ? (double) correctCount / total * 100 : 0;

        String determinedLevel = determineLevelByScore(scoreRate);

        test.setStatus(DiagnosisTest.DiagnosisStatus.COMPLETED.name());
        test.setCompletedAt(LocalDateTime.now());
        test.setCorrectCount((int) correctCount);
        test.setScoreRateFromDouble(scoreRate);
        test.setDeterminedLevel(determinedLevel);
        diagnosisMapper.update(test);

        // 학생 레벨 업데이트
        studentMapper.findById(test.getStudentId()).ifPresent(student -> {
            studentMapper.updateLevel(student.getStudentId(), determinedLevel);
            studentMapper.updateDiagnosisAt(student.getStudentId());
        });

        return Map.of(
                "testId", testId,
                "scoreRate", scoreRate,
                "correctCount", correctCount,
                "totalQuestions", total,
                "determinedLevel", determinedLevel,
                "message", "진단 테스트가 완료되었습니다. 배정된 레벨: " + determinedLevel
        );
    }

    public String determineLevelByScore(double scoreRate) {
        if (scoreRate >= 80) {
            return Student.StudentLevel.A.name();
        } else if (scoreRate >= 50) {
            return Student.StudentLevel.B.name();
        } else {
            return Student.StudentLevel.C.name();
        }
    }

    private DiagnosisStartResponse.ProblemDto mapToProblemDto(Problem problem) {
        List<DiagnosisStartResponse.OptionDto> options = problemMapper
                .findOptionsByProblemId(problem.getProblemId())
                .stream()
                .map(opt -> DiagnosisStartResponse.OptionDto.builder()
                        .optionNo(opt.getOptionNo())
                        .optionText(opt.getOptionText())
                        .optionImgUrl(opt.getOptionImgUrl())
                        .build())
                .collect(Collectors.toList());

        return DiagnosisStartResponse.ProblemDto.builder()
                .problemId(problem.getProblemId())
                .questionText(problem.getQuestionText())
                .questionImgUrl(problem.getQuestionImgUrl())
                .problemType(problem.getProblemType())
                .options(options)
                .estimatedTime(problem.getEstimatedTime())
                .build();
    }
}
