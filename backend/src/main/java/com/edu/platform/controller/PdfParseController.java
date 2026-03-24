package com.edu.platform.controller;

import com.edu.platform.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import com.edu.platform.service.PdfImageExtractService;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class PdfParseController {

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    private final RestTemplate restTemplate;
    private final PdfImageExtractService pdfImageExtractService;

    @PostMapping("/parse-pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> parsePdf(
            @RequestBody Map<String, String> body) {

        String base64Data = body.get("base64Data");
        if (base64Data == null || base64Data.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("base64Data가 필요합니다."));
        }

        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Anthropic API 키가 설정되지 않았습니다. application.yml에 anthropic.api-key를 설정해주세요."));
        }

        try {
            // 1. 텍스트 파싱
            List<Map<String, Object>> problems = callAnthropicApi(base64Data);

            // 2. 그림 영역 추출 및 S3 업로드 (비동기 처리 - 실패해도 파싱 결과는 반환)
            try {
                Map<String, String> imageUrls = pdfImageExtractService.extractDiagramImages(base64Data);
                // 문제번호 기준으로 imageUrl 매핑
                // AI가 반환한 problemNo(PDF 문제 번호)와 row(순서) 둘 다 시도
                for (Map<String, Object> problem : problems) {
                    String rowNo = String.valueOf(problem.get("row"));
                    String problemNo = String.valueOf(problem.get("problemNo"));
                    String matched = imageUrls.getOrDefault(problemNo,
                                     imageUrls.getOrDefault(rowNo, null));
                    if (matched != null) {
                        problem.put("questionImgUrl", matched);
                        log.info("이미지 매핑 완료 - 문제번호:{} row:{} url:{}", problemNo, rowNo, matched);
                    }
                }
                log.info("이미지 추출 완료 - {}개 그림 감지", imageUrls.size());
            } catch (Exception imgEx) {
                log.warn("이미지 추출 실패 (파싱 결과는 유지): {}", imgEx.getMessage());
            }

            return ResponseEntity.ok(ApiResponse.success(problems));
        } catch (Exception e) {
            log.error("PDF 파싱 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("PDF 파싱 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> callAnthropicApi(String base64Data) {
        String prompt = """
                다음 PDF 파일은 수학 문제지입니다. 모든 문제를 추출하여 아래 JSON 배열 형식으로만 응답하세요. 다른 설명은 일절 하지 마세요.
                
                [
                  {
                    "problemNo": "문제지에 표시된 문제 번호(숫자)",
                    "subject": "수학",
                    "grade": "GRADE_1, GRADE_2, GRADE_3 중 하나 (중1→GRADE_1, 중2→GRADE_2, 중3→GRADE_3)",
                    "unitName": "단원명",
                    "questionText": "문제 전체 텍스트",
                    "problemType": "MULTIPLE_CHOICE 또는 SHORT_ANSWER",
                    "level": "A, B, C 중 하나 (난이도 기반 추정: 어려우면 A, 보통이면 B, 쉬우면 C)",
                    "answer": "정답 (객관식은 번호, 주관식은 답)",
                    "explanation": "풀이 해설 (있는 경우)",
                    "options": ["①번지문", "②번지문", "③번지문", "④번지문", "⑤번지문"]
                  }
                ]
                
                객관식이 아닌 경우 options는 빈 배열 []로 설정하세요.
                정답을 알 수 없는 경우 answer는 빈 문자열로 설정하세요.
                반드시 유효한 JSON 배열만 반환하세요.
                """;

        // 요청 바디 구성
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", "claude-sonnet-4-20250514");
        requestBody.put("max_tokens", 8192);

        Map<String, Object> documentContent = new LinkedHashMap<>();
        documentContent.put("type", "document");
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("type", "base64");
        source.put("media_type", "application/pdf");
        source.put("data", base64Data);
        documentContent.put("source", source);

        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(documentContent, textContent));

        requestBody.put("messages", List.of(message));

        // HTTP 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.anthropic.com/v1/messages",
                HttpMethod.POST,
                entity,
                Map.class
        );

        // 응답 파싱
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) throw new RuntimeException("Anthropic API 응답이 비어있습니다.");

        List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
        if (content == null || content.isEmpty()) throw new RuntimeException("AI 응답 content가 없습니다.");

        String text = content.stream()
                .filter(c -> "text".equals(c.get("type")))
                .map(c -> (String) c.get("text"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("AI 텍스트 응답이 없습니다."));

        // JSON 파싱
        String cleaned = text.replaceAll("```json", "").replaceAll("```", "").trim();

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            List<Map<String, Object>> parsed = mapper.readValue(cleaned,
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            // row 번호 추가, problemNo 없으면 row로 대체
            for (int i = 0; i < parsed.size(); i++) {
                parsed.get(i).put("row", i + 1);
                if (!parsed.get(i).containsKey("problemNo") || parsed.get(i).get("problemNo") == null) {
                    parsed.get(i).put("problemNo", String.valueOf(i + 1));
                }
            }
            return parsed;
        } catch (Exception e) {
            log.error("JSON 파싱 실패. AI 응답: {}", cleaned);
            throw new RuntimeException("AI 응답을 JSON으로 파싱할 수 없습니다.");
        }
    }
}
