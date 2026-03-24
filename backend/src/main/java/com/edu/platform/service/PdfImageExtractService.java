package com.edu.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfImageExtractService {

    private final S3Service s3Service;
    private final RestTemplate restTemplate;

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    /**
     * PDF base64 데이터를 받아 각 페이지를 이미지로 변환 후
     * Claude Vision으로 그림 영역 비율을 감지하여 크롭 후 S3에 업로드
     * @return Map<문제번호(String), S3 이미지 URL>
     */
    public Map<String, String> extractDiagramImages(String pdfBase64) {
        Map<String, String> result = new LinkedHashMap<>();

        try {
            byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFRenderer renderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();

                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    // 페이지를 300 DPI 원본 이미지로 변환 (크롭용)
                    BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, 300);

                    // Claude Vision 전송용 750x1000 리사이즈
                    BufferedImage resized = resizeImage(pageImage, 750, 1000);
                    String pageBase64 = bufferedImageToBase64(resized);
                    log.info("페이지 {} - 원본 {}x{}, Vision용 {}x{}",
                            pageIndex + 1, pageImage.getWidth(), pageImage.getHeight(),
                            resized.getWidth(), resized.getHeight());

                    // Claude Vision으로 그림 영역 비율 감지 (리사이즈 이미지 기준)
                    List<Map<String, Object>> diagrams = detectDiagrams(pageBase64, pageIndex + 1);

                    // 크롭은 원본 300DPI 이미지 기준으로 비율 적용
                    int imgW = pageImage.getWidth();
                    int imgH = pageImage.getHeight();

                    for (Map<String, Object> diagram : diagrams) {
                        try {
                            String problemNo = String.valueOf(diagram.get("problemNo"));
                            int x      = clamp((int)(toDouble(diagram.get("x_ratio"))      * imgW), 0, imgW - 1);
                            int y      = clamp((int)(toDouble(diagram.get("y_ratio"))      * imgH), 0, imgH - 1);
                            int width  = clamp((int)(toDouble(diagram.get("width_ratio"))  * imgW), 1, imgW - x);
                            int height = clamp((int)(toDouble(diagram.get("height_ratio")) * imgH), 1, imgH - y);
                            log.info("크롭 픽셀 변환 - 문제 {} | 원본크기 {}x{} | x={} y={} width={} height={}",
                                    problemNo, imgW, imgH, x, y, width, height);

                            BufferedImage cropped = pageImage.getSubimage(x, y, width, height);
                            byte[] croppedBytes = bufferedImageToBytes(cropped);

                            String url = s3Service.uploadImage(croppedBytes, "problem-images");
                            result.put(problemNo, url);
                            log.info("그림 크롭 완료 - 문제 {} [{},{} {}x{}]: {}", problemNo, x, y, width, height, url);

                        } catch (Exception e) {
                            log.warn("그림 크롭 실패 - diagram: {}, error: {}", diagram, e.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("PDF 이미지 추출 실패: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Claude Vision으로 페이지 이미지에서 그림 영역을 비율(0~1)로 감지
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> detectDiagrams(String pageBase64, int pageNumber) {
        String prompt = """
                이 이미지는 수학 문제지입니다.
                도형, 원, 그래프 등 그림이 있는 문제 번호와 그 그림의 위치를 알려주세요.

                중요: 그림(도형)만 포함하는 영역을 지정하세요.
                선지(①②③④⑤)나 텍스트는 절대 포함하지 마세요.

                좌표는 이미지 전체 크기 대비 비율(0.0~1.0)로 지정하세요.
                그림 주변에 3% 여백을 추가하세요.

                y_ratio는 문제 번호와 텍스트가 끝나고 도형이 시작되는 지점으로 설정하세요.
                height_ratio는 도형의 가장 아래쪽 픽셀까지만 포함하세요.
                선지(① ② ③ ④ ⑤)가 시작되는 지점 바로 위에서 끝내세요.

                예시: 21번 문제에 원 두 개짜리 그림이 있다면
                x_ratio=0.05, y_ratio=0.55, width_ratio=0.45, height_ratio=0.25 처럼 그림만 포함

                JSON만 반환: [{"problemNo": "21", "x_ratio": 0.05, "y_ratio": 0.55, "width_ratio": 0.45, "height_ratio": 0.25}]
                """;

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", "claude-sonnet-4-20250514");
            requestBody.put("max_tokens", 1000);

            Map<String, Object> imageContent = new LinkedHashMap<>();
            imageContent.put("type", "image");
            Map<String, Object> imageSource = new LinkedHashMap<>();
            imageSource.put("type", "base64");
            imageSource.put("media_type", "image/png");
            imageSource.put("data", pageBase64);
            imageContent.put("source", imageSource);

            Map<String, Object> textContent = new LinkedHashMap<>();
            textContent.put("type", "text");
            textContent.put("text", prompt);

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", List.of(imageContent, textContent));

            requestBody.put("messages", List.of(message));

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

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) return Collections.emptyList();

            List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
            if (content == null || content.isEmpty()) return Collections.emptyList();

            String text = content.stream()
                    .filter(c -> "text".equals(c.get("type")))
                    .map(c -> (String) c.get("text"))
                    .findFirst().orElse("[]");

            String cleaned = text.replaceAll("```json", "").replaceAll("```", "").trim();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(cleaned,
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

        } catch (Exception e) {
            log.warn("페이지 {} 그림 감지 실패: {}", pageNumber, e.getMessage());
            return Collections.emptyList();
        }
    }

    private BufferedImage resizeImage(BufferedImage source, int targetW, int targetH) {
        BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = resized.createGraphics();
        g.drawImage(source.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();
        return resized;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(val, max));
    }

    private String bufferedImageToBase64(BufferedImage image) throws Exception {
        byte[] bytes = bufferedImageToBytes(image);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] bufferedImageToBytes(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

}
