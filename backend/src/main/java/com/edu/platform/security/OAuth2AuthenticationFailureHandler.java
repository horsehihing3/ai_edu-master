package com.edu.platform.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * [2026-04-04] OAuth2 인증 실패 시 프론트엔드 로그인 페이지로 에러 메시지와 함께 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Value("${app.base-url:http://localhost:7001}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        authorizationRequestRepository.removeAuthorizationRequest(request, response);

        log.warn("OAuth2 로그인 실패: {}", exception.getMessage());

        String errorMessage = URLEncoder.encode(
                exception.getMessage() != null ? exception.getMessage() : "소셜 로그인에 실패했습니다.",
                StandardCharsets.UTF_8);

        String redirectUrl = frontendBaseUrl + "/login?error=" + errorMessage;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
