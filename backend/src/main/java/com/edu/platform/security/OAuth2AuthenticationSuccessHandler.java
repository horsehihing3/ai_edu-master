package com.edu.platform.security;

import com.edu.platform.config.JwtProvider;
import com.edu.platform.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * [2026-04-04] OAuth2 인증 성공 시 JWT 발급 후 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Value("${app.base-url:http://localhost:7001}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtProvider.generateAccessToken(userDetails);

        // 쿠키 정리
        authorizationRequestRepository.removeAuthorizationRequest(request, response);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl + "/oauth2/callback")
                .queryParam("token", accessToken)
                .build().toUriString();

        log.info("OAuth2 로그인 성공: {} → {}", user.getEmail(), redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
