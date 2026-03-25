package com.edu.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfImageExtractService {

    private final S3Service s3Service;
    private final RestTemplate restTemplate;

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    // Python 스크립트 경로 (백엔드 루트 기준)
    private static final String SCRIPT_PATH = "scripts/extract_images.py";

    /**
     * PyMuPDF로 PDF 내 이미지 추출 → Vision으로 문제번호 매핑 → S3 업로드
     * key = 문제번호(string), value = S3 URL
     */
    public Map<String, String> extractDiagramImages(String pdfBase64) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> extracted = runPythonExtract(pdfBase64);
            if (extracted.isEmpty()) {
                log.info("PyMuPDF 추출 결과 없음 — 이미지 없는 PDF");
                return result;
            }
            log.info("PyMuPDF 추출 완료 — {}개 이미지", extracted.size());

            // 페이지별로 그룹화
            Map<Integer, List<Map<String, Object>>> byPage = new LinkedHashMap<>();
            for (Map<String, Object> item : extracted) {
                int page = ((Number) item.get("page")).intValue();
                byPage.computeIfAbsent(page, k -> new ArrayList<>()).add(item);
            }

            // 페이지별 처리
            for (Map.Entry<Integer, List<Map<String, Object>>> entry : byPage.entrySet()) {
                int pageNumber = entry.getKey();
                List<Map<String, Object>> pageImages = entry.getValue();
                log.info("페이지 {} — {}개 이미지 처리 시작", pageNumber, pageImages.size());

                // Vision 컨텍스트용 (1000px 리사이즈 base64)
                String pageBase64 = renderPageFromPdf(pdfBase64, pageNumber);
                // 크롭용 고해상도 BufferedImage (200 DPI)
                java.awt.image.BufferedImage fullPageImage = renderPageAsBufferedImage(pdfBase64, pageNumber);

                // 1) 각 이미지의 문제번호 식별
                Map<String, List<Map<String, Object>>> problemImageMap = new LinkedHashMap<>();

                for (Map<String, Object> imgInfo : pageImages) {
                    try {
                        Map<String, Object> bbox = (Map<String, Object>) imgInfo.get("bbox");
                        double pageWidth  = ((Number) imgInfo.get("pageWidth")).doubleValue();
                        double pageHeight = ((Number) imgInfo.get("pageHeight")).doubleValue();

                        double xRatio = toDouble(bbox.get("x0")) / pageWidth;
                        double yRatio = toDouble(bbox.get("y0")) / pageHeight;
                        double wRatio = (toDouble(bbox.get("x1")) - toDouble(bbox.get("x0"))) / pageWidth;
                        double hRatio = (toDouble(bbox.get("y1")) - toDouble(bbox.get("y0"))) / pageHeight;

                        String problemNo = identifyProblemNoFromPage(
                            pageBase64, (String) imgInfo.get("imageBase64"),
                            pageNumber, xRatio, yRatio, wRatio, hRatio);

                        if (problemNo == null || problemNo.isBlank()) {
                            log.warn("페이지 {} — 문제번호 식별 실패 (yRatio={}), skip",
                                pageNumber, String.format("%.2f", yRatio));
                            continue;
                        }

                        problemImageMap.computeIfAbsent(problemNo, k -> new ArrayList<>()).add(imgInfo);
                        log.info("페이지:{} 문제:{} 이미지 추가 (총 {}개)",
                            pageNumber, problemNo, problemImageMap.get(problemNo).size());

                    } catch (Exception e) {
                        log.warn("페이지 {} 이미지 처리 실패: {}", pageNumber, e.getMessage());
                    }
                }

                // 2) 문제번호별 bbox 합산 → fullPageImage에서 크롭 → S3 업로드
                for (Map.Entry<String, List<Map<String, Object>>> pe : problemImageMap.entrySet()) {
                    String problemNo = pe.getKey();
                    List<Map<String, Object>> imgs = pe.getValue();

                    try {
                        // 모든 bbox 합산 (union)
                        double minX0 = Double.MAX_VALUE, minY0 = Double.MAX_VALUE;
                        double maxX1 = -Double.MAX_VALUE, maxY1 = -Double.MAX_VALUE;
                        double pageWidth = 0, pageHeight = 0;

                        for (Map<String, Object> imgInfo : imgs) {
                            Map<String, Object> bbox = (Map<String, Object>) imgInfo.get("bbox");
                            minX0 = Math.min(minX0, toDouble(bbox.get("x0")));
                            minY0 = Math.min(minY0, toDouble(bbox.get("y0")));
                            maxX1 = Math.max(maxX1, toDouble(bbox.get("x1")));
                            maxY1 = Math.max(maxY1, toDouble(bbox.get("y1")));
                            pageWidth  = toDouble(imgInfo.get("pageWidth"));
                            pageHeight = toDouble(imgInfo.get("pageHeight"));
                        }

                        // 5% 여백 추가
                        double marginX = (maxX1 - minX0) * 0.05;
                        double marginY = (maxY1 - minY0) * 0.05;
                        minX0 = Math.max(0, minX0 - marginX);
                        minY0 = Math.max(0, minY0 - marginY);
                        maxX1 = Math.min(pageWidth,  maxX1 + marginX);
                        maxY1 = Math.min(pageHeight, maxY1 + marginY);

                        byte[] croppedBytes;
                        if (fullPageImage != null && pageWidth > 0 && pageHeight > 0) {
                            // PDF 좌표(pt) → 픽셀 변환 (렌더링 이미지 크기 기준)
                            double scaleX = (double) fullPageImage.getWidth()  / pageWidth;
                            double scaleY = (double) fullPageImage.getHeight() / pageHeight;

                            int cx0 = (int) Math.max(0, minX0 * scaleX);
                            int cy0 = (int) Math.max(0, minY0 * scaleY);
                            int cx1 = (int) Math.min(fullPageImage.getWidth(),  maxX1 * scaleX);
                            int cy1 = (int) Math.min(fullPageImage.getHeight(), maxY1 * scaleY);

                            java.awt.image.BufferedImage cropped =
                                fullPageImage.getSubimage(cx0, cy0, cx1 - cx0, cy1 - cy0);

                            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                            javax.imageio.ImageIO.write(cropped, "PNG", baos);
                            croppedBytes = baos.toByteArray();
                            log.info("페이지:{} 문제:{} — bbox 크롭 완료 [({},{})→({},{})] {}x{}px",
                                pageNumber, problemNo, cx0, cy0, cx1, cy1, cx1 - cx0, cy1 - cy0);
                        } else {
                            // fallback: 첫 번째 이미지 사용
                            croppedBytes = Base64.getDecoder().decode((String) imgs.get(0).get("imageBase64"));
                            log.warn("페이지:{} 문제:{} — 크롭 불가, fallback 이미지 사용", pageNumber, problemNo);
                        }

                        String url = s3Service.uploadImage(croppedBytes, "problem-images");
                        result.put(problemNo, url);
                        log.info("완료 — 페이지:{} 문제:{} 조각수:{} URL:{}",
                            pageNumber, problemNo, imgs.size(), url);

                    } catch (Exception e) {
                        log.warn("페이지:{} 문제:{} 크롭/업로드 실패: {}", pageNumber, problemNo, e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.error("PDF 이미지 추출 실패: {}", e.getMessage(), e);
        }
        return result;
    }

    /**
     * PDF에서 특정 페이지를 렌더링해서 base64로 반환 (Vision 컨텍스트용)
     */
    private String renderPageFromPdf(String pdfBase64, int pageNumber) {
        try {
            byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
            org.apache.pdfbox.pdmodel.PDDocument document =
                org.apache.pdfbox.Loader.loadPDF(pdfBytes);
            org.apache.pdfbox.rendering.PDFRenderer renderer =
                new org.apache.pdfbox.rendering.PDFRenderer(document);
            java.awt.image.BufferedImage pageImage =
                renderer.renderImageWithDPI(pageNumber - 1, 150);
            document.close();

            // 1000px 너비로 리사이즈
            int targetW = 1000;
            double ratio = (double) targetW / pageImage.getWidth();
            int targetH = (int)(pageImage.getHeight() * ratio);
            java.awt.image.BufferedImage resized =
                new java.awt.image.BufferedImage(targetW, targetH, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = resized.createGraphics();
            g.drawImage(pageImage, 0, 0, targetW, targetH, null);
            g.dispose();

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(resized, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.warn("페이지 {} 렌더링 실패: {}", pageNumber, e.getMessage());
            return null;
        }
    }

    /**
     * PDF 특정 페이지를 200DPI BufferedImage로 렌더링해서 반환 (크롭용)
     */
    private java.awt.image.BufferedImage renderPageAsBufferedImage(String pdfBase64, int pageNumber) {
        try {
            byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
            org.apache.pdfbox.pdmodel.PDDocument document =
                org.apache.pdfbox.Loader.loadPDF(pdfBytes);
            org.apache.pdfbox.rendering.PDFRenderer renderer =
                new org.apache.pdfbox.rendering.PDFRenderer(document);
            java.awt.image.BufferedImage pageImage =
                renderer.renderImageWithDPI(pageNumber - 1, 200);
            document.close();
            return pageImage;
        } catch (Exception e) {
            log.warn("페이지 {} BufferedImage 렌더링 실패: {}", pageNumber, e.getMessage());
            return null;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Python 스크립트 실행
    // ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> runPythonExtract(String pdfBase64) throws Exception {
        // 스크립트 경로 확인
        Path scriptPath = Paths.get(SCRIPT_PATH);
        if (!Files.exists(scriptPath)) {
            // 절대경로로 재시도
            scriptPath = Paths.get(System.getProperty("user.dir"), SCRIPT_PATH);
        }
        if (!Files.exists(scriptPath)) {
            throw new RuntimeException("Python 스크립트를 찾을 수 없습니다: " + scriptPath.toAbsolutePath());
        }

        // base64를 임시 파일로 저장 (커맨드라인 길이 제한 우회)
        Path tempFile = Files.createTempFile("pdf_b64_", ".txt");
        try {
            Files.writeString(tempFile, pdfBase64);

            ProcessBuilder pb = new ProcessBuilder(
                    "python", scriptPath.toAbsolutePath().toString(),
                    tempFile.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(false);
            Process process = pb.start();

            // stdout 읽기
            String stdout = new String(process.getInputStream().readAllBytes());
            String stderr = new String(process.getErrorStream().readAllBytes());

            int exitCode = process.waitFor();

            if (!stderr.isBlank()) {
                log.warn("Python stderr: {}", stderr.trim());
            }

            if (exitCode != 0) {
                throw new RuntimeException("Python 스크립트 실패 (exit=" + exitCode + "): " + stderr);
            }

            log.debug("Python stdout length: {}", stdout.length());

            ObjectMapper mapper = new ObjectMapper();
            Object parsed = mapper.readValue(stdout.trim(), Object.class);

            // 에러 응답 처리
            if (parsed instanceof Map) {
                Map<String, Object> errorMap = (Map<String, Object>) parsed;
                if (errorMap.containsKey("error")) {
                    throw new RuntimeException("Python 오류: " + errorMap.get("error"));
                }
            }

            return mapper.readValue(stdout.trim(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 이미지 합치기 / Vision
    // ──────────────────────────────────────────────────────────────

    /**
     * 여러 이미지 조각을 세로로 합치기
     * - 각 조각의 원본 비율 유지 (늘리지 않음)
     * - 너비가 다른 조각은 가장 넓은 너비 기준으로 흰색 패딩 추가
     * - y 좌표 기준으로 정렬 후 합치기
     */
    private byte[] mergeImagesVertically(List<Map<String, Object>> imgInfoList) throws Exception {
        // y0 기준으로 정렬 (위에서 아래 순서로)
        List<Map<String, Object>> sorted = new ArrayList<>(imgInfoList);
        sorted.sort((a, b) -> {
            Map<String, Object> bboxA = (Map<String, Object>) a.get("bbox");
            Map<String, Object> bboxB = (Map<String, Object>) b.get("bbox");
            double y0A = bboxA != null ? toDouble(bboxA.get("y0")) : 0;
            double y0B = bboxB != null ? toDouble(bboxB.get("y0")) : 0;
            return Double.compare(y0A, y0B);
        });

        List<java.awt.image.BufferedImage> images = new ArrayList<>();
        int maxWidth = 0;
        int totalHeight = 0;

        for (Map<String, Object> imgInfo : sorted) {
            String b64 = (String) imgInfo.get("imageBase64");
            byte[] bytes = Base64.getDecoder().decode(b64);
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(
                new java.io.ByteArrayInputStream(bytes));
            if (img != null) {
                images.add(img);
                maxWidth = Math.max(maxWidth, img.getWidth());
                totalHeight += img.getHeight();
            }
        }

        if (images.isEmpty()) throw new RuntimeException("합칠 이미지 없음");

        // 캔버스 생성 (흰색 배경)
        java.awt.image.BufferedImage merged =
            new java.awt.image.BufferedImage(maxWidth, totalHeight,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = merged.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
            java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, maxWidth, totalHeight);

        // 각 조각을 원본 크기 그대로 가운데 정렬로 배치 (비율 유지, 늘리지 않음)
        int y = 0;
        for (java.awt.image.BufferedImage img : images) {
            int x = (maxWidth - img.getWidth()) / 2;
            g.drawImage(img, x, y, img.getWidth(), img.getHeight(), null);
            y += img.getHeight();
        }
        g.dispose();

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(merged, "PNG", baos);
        return baos.toByteArray();
    }

    private double toDouble(Object val) {
        return val instanceof Number ? ((Number) val).doubleValue() : 0;
    }

    /**
     * 페이지 전체 이미지 + 그림 위치 비율로 문제번호 식별
     */
    @SuppressWarnings("unchecked")
    private String identifyProblemNoFromPage(String pageBase64, String imgBase64,
            int pageNumber, double xRatio, double yRatio, double wRatio, double hRatio) {

        String prompt = String.format("""
                This is page %d of a math exam.

                A figure (geometric shape/graph/diagram) has been extracted from this page.
                The figure is located at approximately:
                - Left edge: %.0f%% from left
                - Top edge: %.0f%% from top
                - Width: %.0f%% of page width
                - Height: %.0f%% of page height

                Look at the page image and find which problem number contains a figure at that location.

                Reply with ONLY the problem number as a single integer. Nothing else.
                If you cannot determine it, reply with: 0

                Examples: 12  or  15  or  0
                """,
                pageNumber,
                xRatio * 100, yRatio * 100,
                wRatio * 100, hRatio * 100);

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", "claude-sonnet-4-20250514");
            requestBody.put("max_tokens", 10);

            List<Object> contentList = new ArrayList<>();

            // 페이지 전체 이미지 추가 (컨텍스트)
            if (pageBase64 != null) {
                Map<String, Object> pageImageContent = new LinkedHashMap<>();
                pageImageContent.put("type", "image");
                Map<String, Object> pageSource = new LinkedHashMap<>();
                pageSource.put("type", "base64");
                pageSource.put("media_type", "image/png");
                pageSource.put("data", pageBase64);
                pageImageContent.put("source", pageSource);
                contentList.add(pageImageContent);
            }

            // 텍스트 프롬프트
            Map<String, Object> textContent = new LinkedHashMap<>();
            textContent.put("type", "text");
            textContent.put("text", prompt);
            contentList.add(textContent);

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", contentList);
            requestBody.put("messages", List.of(message));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", anthropicApiKey);
            headers.set("anthropic-version", "2023-06-01");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.anthropic.com/v1/messages",
                    HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) return null;

            List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
            if (content == null || content.isEmpty()) return null;

            String text = content.stream()
                    .filter(c -> "text".equals(c.get("type")))
                    .map(c -> (String) c.get("text"))
                    .findFirst().orElse("0").trim();

            String number = text.replaceAll("[^0-9]", "").trim();
            log.info("Vision 문제번호 식별 — 페이지:{} 위치:[x={} y={}] → '{}'",
                pageNumber,
                String.format("%.2f", xRatio),
                String.format("%.2f", yRatio),
                number);

            if (number.isBlank() || number.equals("0")) return null;
            return number;

        } catch (Exception e) {
            log.warn("Vision 문제번호 식별 실패 — 페이지:{}: {}", pageNumber, e.getMessage());
            return null;
        }
    }
}
