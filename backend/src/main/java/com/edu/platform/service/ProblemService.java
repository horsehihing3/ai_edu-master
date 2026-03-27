package com.edu.platform.service;

import com.edu.platform.domain.Problem;
import com.edu.platform.domain.ProblemOption;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.ProblemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemMapper problemMapper;

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> searchProblems(Map<String, Object> searchParams, int page, int size) {
        int offset = page * size;
        int limit = size;

        String level = searchParams != null ? (String) searchParams.get("level") : null;
        String grade = searchParams != null ? (String) searchParams.get("grade") : null;
        String unitName = searchParams != null ? (String) searchParams.get("unitName") : null;
        String keyword = searchParams != null ? (String) searchParams.get("keyword") : null;
        Long schoolId = searchParams != null && searchParams.get("schoolId") != null
                ? Long.valueOf(searchParams.get("schoolId").toString()) : null;

        List<Problem> problems = problemMapper.search(level, grade, unitName, schoolId, keyword, offset, limit);
        long totalElements = problemMapper.countSearch(level, grade, unitName, schoolId, keyword);
        int totalPages = (int) Math.ceil((double) totalElements / limit);

        List<Map<String, Object>> content = problems.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, totalElements, totalPages, page, limit);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProblemDetail(Long problemId) {
        Problem problem = problemMapper.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        return mapToDetailDto(problem);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProblemsForDiagnosis(String level, int count) {
        List<Problem> problems = problemMapper.findForDiagnosis(level, count);
        return problems.stream().map(this::mapToSummaryDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProblemsForAssignment(Long assignmentId) {
        List<Problem> problems = problemMapper.findByAssignmentId(assignmentId);
        return problems.stream().map(this::mapToSummaryDto).collect(Collectors.toList());
    }

    @Transactional
    public Long createProblem(Map<String, Object> request, Long createdBy) {
        Problem problem = buildProblemFromRequest(request);
        problem.setCreatedBy(createdBy);
        problemMapper.insert(problem);

        List<?> options = (List<?>) request.get("options");
        if (options != null) {
            saveOptions(problem.getProblemId(), options);
        }
        return problem.getProblemId();
    }

    @Transactional
    public void updateProblem(Long problemId, Map<String, Object> request) {
        problemMapper.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        Problem problem = buildProblemFromRequest(request);
        problem.setProblemId(problemId);
        problemMapper.update(problem);

        // 선지 삭제 후 재저장
        problemMapper.deleteOptionsByProblemId(problemId);
        List<?> options = (List<?>) request.get("options");
        if (options != null && !options.isEmpty()) {
            saveOptions(problemId, options);
        }
    }

    @Transactional
    public void deleteProblem(Long problemId) {
        problemMapper.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));
        problemMapper.deleteById(problemId);
    }

    // [2026-03-26] 배치 업로드 시 question_text 기준 중복 감지 → duplicate_problem_id 저장
    @Transactional
    public Map<String, Object> uploadBatch(List<Map<String, Object>> problems, Long createdBy) {
        int success = 0;
        int duplicateCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < problems.size(); i++) {
            try {
                Map<String, Object> req = problems.get(i);
                String questionText = toString(req.get("questionText"));

                // question_text 기준 중복 체크
                Long duplicateProblemId = null;
                if (questionText != null && !questionText.isBlank()) {
                    duplicateProblemId = problemMapper.findIdByQuestionText(questionText);
                }

                Problem problem = buildProblemFromRequest(req);
                problem.setCreatedBy(createdBy);
                problem.setDuplicateProblemId(duplicateProblemId); // 중복이면 원본 ID 세팅, 없으면 null

                problemMapper.insert(problem);

                List<?> options = (List<?>) req.get("options");
                if (options != null) {
                    saveOptions(problem.getProblemId(), options);
                }

                if (duplicateProblemId != null) {
                    duplicateCount++;
                    log.info("중복 문제 감지 - 새 problem_id: {}, 원본 problem_id: {}", problem.getProblemId(), duplicateProblemId);
                }
                success++;
            } catch (Exception e) {
                errors.add("row " + (i + 1) + ": " + e.getMessage());
                log.warn("배치 업로드 실패 - row {}: {}", i + 1, e.getMessage());
            }
        }

        return Map.of(
                "total", problems.size(),
                "success", success,
                "failed", problems.size() - success,
                "duplicates", duplicateCount,
                "errors", errors
        );
    }

    private Problem buildProblemFromRequest(Map<String, Object> req) {
        String source = toString(req.get("source"));
        String problemType = toString(req.get("problemType"));
        String answer = toString(req.get("answer"));
        Integer difficulty = toInt(req.get("difficulty"));

        return Problem.builder()
                .schoolId(toLong(req.get("schoolId")))
                .subjectCodeId(toLong(req.get("subjectCodeId")))
                .unitCodeId(toLong(req.get("unitCodeId")))
                .level(toString(req.get("level")))
                .grade(toString(req.get("grade")))
                .source(source != null ? source : "CUSTOM")
                .sourceDetail(toString(req.get("sourceDetail")))
                .problemType(problemType != null ? problemType : "MULTIPLE_CHOICE")
                .questionText(toString(req.get("questionText")))
                .questionImgUrl(toString(req.get("questionImgUrl")))
                .answer(answer != null ? answer : "")
                .explanation(toString(req.get("explanation")))
                .hint(toString(req.get("hint")))
                .difficulty(difficulty != null ? difficulty : 3)
                .estimatedTime(toInt(req.get("estimatedTime")))
                .passage(toString(req.get("passage")))
                .passageImgUrl(toString(req.get("passageImgUrl")))
                .build();
    }

    @SuppressWarnings("unchecked")
    private void saveOptions(Long problemId, List<?> rawOptions) {
        for (int i = 0; i < rawOptions.size(); i++) {
            Map<String, Object> opt = (Map<String, Object>) rawOptions.get(i);
            ProblemOption option = ProblemOption.builder()
                    .problemId(problemId)
                    .optionNo(i + 1)
                    .optionText(toString(opt.get("optionText")))
                    .optionImgUrl(toString(opt.get("optionImgUrl")))
                    .build();
            problemMapper.insertOption(option);
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) { return null; }
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) { return null; }
    }

    private String toString(Object val) {
        return val != null ? val.toString() : null;
    }

    private Map<String, Object> mapToSummaryDto(Problem p) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("problemId", p.getProblemId());
        map.put("subject", p.getSubject() != null ? p.getSubject() : "");
        map.put("questionText", p.getQuestionText() != null ? p.getQuestionText() : "");
        map.put("questionImgUrl", p.getQuestionImgUrl() != null ? p.getQuestionImgUrl() : "");
        map.put("level", p.getLevel() != null ? p.getLevel() : "");
        map.put("grade", p.getGrade() != null ? p.getGrade() : "");
        map.put("unitName", p.getUnitName() != null ? p.getUnitName() : "");
        map.put("problemType", p.getProblemType() != null ? p.getProblemType() : "");
        map.put("difficulty", p.getDifficulty() != null ? p.getDifficulty() : 0);
        map.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
        // [2026-03-26] 중복 문제 ID 추가
        map.put("duplicateProblemId", p.getDuplicateProblemId());
        return map;
    }

    private Map<String, Object> mapToDetailDto(Problem p) {
        List<ProblemOption> rawOptions = problemMapper.findOptionsByProblemId(p.getProblemId());
        List<Map<String, Object>> options = rawOptions.stream()
                .map(opt -> Map.<String, Object>of(
                        "optionId", opt.getOptionId(),
                        "optionNo", opt.getOptionNo(),
                        "optionText", opt.getOptionText() != null ? opt.getOptionText() : "",
                        "optionImgUrl", opt.getOptionImgUrl() != null ? opt.getOptionImgUrl() : ""
                ))
                .collect(Collectors.toList());

        Map<String, Object> detail = new java.util.LinkedHashMap<>();
        detail.put("problemId", p.getProblemId());
        detail.put("subject", p.getSubject() != null ? p.getSubject() : "");
        detail.put("questionText", p.getQuestionText() != null ? p.getQuestionText() : "");
        detail.put("questionImgUrl", p.getQuestionImgUrl() != null ? p.getQuestionImgUrl() : "");
        detail.put("level", p.getLevel() != null ? p.getLevel() : "");
        detail.put("grade", p.getGrade() != null ? p.getGrade() : "");
        detail.put("unitName", p.getUnitName() != null ? p.getUnitName() : "");
        detail.put("problemType", p.getProblemType() != null ? p.getProblemType() : "");
        detail.put("answer", p.getAnswer() != null ? p.getAnswer() : "");
        detail.put("explanation", p.getExplanation() != null ? p.getExplanation() : "");
        detail.put("hint", p.getHint() != null ? p.getHint() : "");
        detail.put("estimatedTime", p.getEstimatedTime() != null ? p.getEstimatedTime() : 0);
        detail.put("options", options);
        // [2026-03-26] 중복 문제 ID 추가
        detail.put("duplicateProblemId", p.getDuplicateProblemId());
        // [2026-03-27] 보기(참고 지문) 추가
        detail.put("passage", p.getPassage() != null ? p.getPassage() : "");
        detail.put("passageImgUrl", p.getPassageImgUrl() != null ? p.getPassageImgUrl() : "");
        return detail;
    }
}
