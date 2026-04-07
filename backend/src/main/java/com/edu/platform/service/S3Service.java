package com.edu.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.cloudfront.domain:}")
    private String cloudfrontDomain;

    public String uploadImage(byte[] imageBytes, String folder) {
        String key = folder + "/" + UUID.randomUUID() + ".png";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/png")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(imageBytes));

        // CloudFront 도메인이 있으면 CDN URL, 없으면 S3 URL 반환
        if (cloudfrontDomain != null && !cloudfrontDomain.isBlank()) {
            return "https://" + cloudfrontDomain + "/" + key;
        }
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
    }

    /**
     * [2026-04-07] S3 객체에 대한 Presigned URL 생성 (유효시간 15분)
     * S3 URL 또는 CloudFront URL에서 key를 추출하여 서명된 URL을 반환.
     * AWS 자격증명 미설정 시 원본 URL을 그대로 반환.
     */
    public String generatePresignedUrl(String originalUrl, int durationMinutes) {
        if (originalUrl == null || originalUrl.isBlank()) return originalUrl;
        try {
            String key = extractKeyFromUrl(originalUrl);
            if (key == null) return originalUrl;

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(durationMinutes))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build())
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.warn("Presigned URL 생성 실패 — 원본 URL 반환: {}", e.getMessage());
            return originalUrl;
        }
    }

    /** S3 URL (https://bucket.s3.region.amazonaws.com/key) 또는 CloudFront URL에서 key 추출 */
    private String extractKeyFromUrl(String url) {
        // CloudFront URL: https://domain/key
        if (cloudfrontDomain != null && !cloudfrontDomain.isBlank() && url.contains(cloudfrontDomain)) {
            return url.substring(url.indexOf(cloudfrontDomain) + cloudfrontDomain.length() + 1);
        }
        // S3 URL: https://bucket.s3.amazonaws.com/key 또는 https://bucket.s3.region.amazonaws.com/key
        if (url.contains(".amazonaws.com/")) {
            return url.substring(url.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
        }
        return null;
    }
}
