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
        // [2026-03-26] 파싱 프롬프트 개선 — 수식 변수 오인 방지, 원문 그대로 추출 강조
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
                    "passage": "보기 — 테두리 박스 안의 참고 지문/조건 텍스트 (없으면 빈 문자열 \"\")",
                    "options": ["①번지문", "②번지문", "③번지문", "④번지문", "⑤번지문"]
                  }
                ]

                【보기(passage) 추출 규칙】
                - 직사각형 테두리선(박스)으로 둘러싸인 참고 내용은 반드시 passage 필드에 추출
                - "보기"라는 글자가 박스 안에 있든 없든 상관없이, 테두리 박스가 있으면 무조건 passage로 추출
                - 테두리 박스 안의 텍스트(조건, 예시, 수식, 기호 목록 등)를 원문 그대로 옮길 것
                - 보기가 없는 문제는 passage를 빈 문자열 ""로 설정
                - 보기 내용을 절대 questionText 안에 포함시키지 말 것 — 반드시 passage 필드에만 넣을 것
                - 보기가 이미지(도형 포함)인 경우에는 passage를 빈 문자열로 두고 questionText에 '[보기그림]' 태그 포함

                【수식 파싱 절대 규칙 — 반드시 준수】
                1. 수식 변수(A, B, C, x, y, n, k 등 알파벳)를 절대 숫자로 변환하지 말 것
                   - 잘못된 예: Ax → 4x, Bx → 6x, An → 4n
                   - 올바른 예: Ax → Ax, Bx → Bx (원문 그대로)
                2. 문제 텍스트는 PDF에 인쇄된 원문을 그대로 옮길 것 (임의 수정 금지)
                3. 수식에서 알파벳 대문자(A~Z)는 변수 또는 계수일 가능성이 높음 — 숫자처럼 보여도 알파벳이면 알파벳으로 표기
                4. √, ², ³ 등 수학 기호는 원문 그대로 유지할 것
                5. 분수, 지수, 루트 표현은 가능한 원문에 가깝게 텍스트로 표현할 것
                   - 예: √(a²+b²), x² - 3x + 2, 1/2, (x+1)/(x-1)
                6. 선지(보기)의 수식도 동일 규칙 적용
                
                【기타 규칙】
                - 객관식이 아닌 경우 options는 빈 배열 []로 설정
                - 정답을 알 수 없는 경우 answer는 빈 문자열로 설정
                - 그림/도형이 포함된 문제는 questionText에 '[그림]' 태그를 포함하여 표시
                - 반드시 유효한 JSON 배열만 반환
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
