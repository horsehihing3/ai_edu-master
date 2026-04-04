package com.edu.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SesClient sesClient;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token, String userName) {
        String verifyLink = baseUrl + "/verify-email?token=" + token;
        String htmlBody = buildVerificationEmailHtml(userName, verifyLink);

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data("[AI 교육 플랫폼] 이메일 인증 안내").charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                    .build())
                            .build())
                    .source(fromEmail)
                    .build();

            sesClient.sendEmail(request);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            // SES 미설정·네트워크 오류 시 가입 흐름을 막지 않음 — 재발송 버튼으로 대체
        }
    }

    private String buildVerificationEmailHtml(String userName, String verifyLink) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f4f6fb;font-family:'Noto Sans KR',sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6fb;padding:40px 0;">
                    <tr><td align="center">
                      <table width="520" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                        <tr>
                          <td style="background:#1E3A8A;padding:32px 40px;text-align:center;">
                            <h1 style="margin:0;color:#fff;font-size:22px;font-weight:700;">AI 교육 플랫폼</h1>
                            <p style="margin:8px 0 0;color:#93C5FD;font-size:14px;">이메일 인증 안내</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;">
                            <p style="margin:0 0 12px;color:#1F2937;font-size:15px;">안녕하세요, <strong>%s</strong>님</p>
                            <p style="margin:0 0 28px;color:#6B7280;font-size:14px;line-height:1.7;">
                              AI 교육 플랫폼에 가입해 주셔서 감사합니다.<br/>
                              아래 버튼을 클릭하여 이메일 인증을 완료해 주세요.<br/>
                              이 링크는 <strong>24시간</strong> 동안 유효합니다.
                            </p>
                            <div style="text-align:center;margin:32px 0;">
                              <a href="%s" style="display:inline-block;background:#1E3A8A;color:#fff;text-decoration:none;padding:14px 36px;border-radius:8px;font-size:15px;font-weight:600;">이메일 인증하기</a>
                            </div>
                            <p style="margin:24px 0 0;color:#9CA3AF;font-size:12px;line-height:1.7;">
                              본인이 가입하지 않으셨다면 이 이메일을 무시하셔도 됩니다.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#F9FAFB;padding:20px 40px;text-align:center;">
                            <p style="margin:0;color:#9CA3AF;font-size:12px;">© 2026 AI 교육 플랫폼. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(userName, verifyLink);
    }

    public void sendPasswordResetEmail(String toEmail, String token, String userName) {
        String resetLink = baseUrl + "/password/reset?token=" + token;
        String htmlBody = buildResetEmailHtml(userName, resetLink);

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data("[AI 교육 플랫폼] 비밀번호 재설정 안내").charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                    .build())
                            .build())
                    .source(fromEmail)
                    .build();

            sesClient.sendEmail(request);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (SesException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    public void sendTempPasswordEmail(String toEmail, String tempPassword, String userName) {
        String htmlBody = buildTempPasswordEmailHtml(userName, tempPassword);

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data("[AI 교육 플랫폼] 임시 비밀번호 발급 안내").charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                    .build())
                            .build())
                    .source(fromEmail)
                    .build();

            sesClient.sendEmail(request);
            log.info("Temp password email sent to: {}", toEmail);
        } catch (SesException e) {
            log.error("Failed to send temp password email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    private String buildTempPasswordEmailHtml(String userName, String tempPassword) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f4f6fb;font-family:'Noto Sans KR',sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6fb;padding:40px 0;">
                    <tr><td align="center">
                      <table width="520" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                        <tr>
                          <td style="background:#1E3A8A;padding:32px 40px;text-align:center;">
                            <h1 style="margin:0;color:#fff;font-size:22px;font-weight:700;">AI 교육 플랫폼</h1>
                            <p style="margin:8px 0 0;color:#93C5FD;font-size:14px;">임시 비밀번호 발급 안내</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;">
                            <p style="margin:0 0 12px;color:#1F2937;font-size:15px;">안녕하세요, <strong>%s</strong>님</p>
                            <p style="margin:0 0 28px;color:#6B7280;font-size:14px;line-height:1.7;">
                              관리자에 의해 임시 비밀번호가 발급되었습니다.<br/>
                              아래 임시 비밀번호로 로그인 후 반드시 비밀번호를 변경해 주세요.
                            </p>
                            <div style="background:#F3F4F6;border-radius:8px;padding:20px;text-align:center;margin:24px 0;">
                              <p style="margin:0 0 8px;color:#6B7280;font-size:13px;">임시 비밀번호</p>
                              <p style="margin:0;color:#1E3A8A;font-size:22px;font-weight:700;letter-spacing:2px;font-family:monospace;">%s</p>
                            </div>
                            <p style="margin:24px 0 0;color:#9CA3AF;font-size:12px;line-height:1.7;">
                              보안을 위해 로그인 후 즉시 비밀번호를 변경해 주세요.<br/>
                              본인이 요청하지 않은 경우 관리자에게 문의해 주세요.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#F9FAFB;padding:20px 40px;text-align:center;">
                            <p style="margin:0;color:#9CA3AF;font-size:12px;">© 2026 AI 교육 플랫폼. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(userName, tempPassword);
    }

    private String buildResetEmailHtml(String userName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f4f6fb;font-family:'Noto Sans KR',sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6fb;padding:40px 0;">
                    <tr><td align="center">
                      <table width="520" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                        <tr>
                          <td style="background:#1E3A8A;padding:32px 40px;text-align:center;">
                            <h1 style="margin:0;color:#fff;font-size:22px;font-weight:700;">AI 교육 플랫폼</h1>
                            <p style="margin:8px 0 0;color:#93C5FD;font-size:14px;">비밀번호 재설정 안내</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;">
                            <p style="margin:0 0 12px;color:#1F2937;font-size:15px;">안녕하세요, <strong>%s</strong>님</p>
                            <p style="margin:0 0 28px;color:#6B7280;font-size:14px;line-height:1.7;">
                              비밀번호 재설정 요청을 받았습니다.<br/>
                              아래 버튼을 클릭하여 새 비밀번호를 설정해 주세요.<br/>
                              이 링크는 <strong>30분간</strong> 유효합니다.
                            </p>
                            <div style="text-align:center;margin:32px 0;">
                              <a href="%s" style="display:inline-block;background:#1E3A8A;color:#fff;text-decoration:none;padding:14px 36px;border-radius:8px;font-size:15px;font-weight:600;">비밀번호 재설정하기</a>
                            </div>
                            <p style="margin:24px 0 0;color:#9CA3AF;font-size:12px;line-height:1.7;">
                              본인이 요청하지 않으셨다면 이 이메일을 무시하셔도 됩니다.<br/>
                              링크를 클릭하지 않는 한 비밀번호는 변경되지 않습니다.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#F9FAFB;padding:20px 40px;text-align:center;">
                            <p style="margin:0;color:#9CA3AF;font-size:12px;">© 2026 AI 교육 플랫폼. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(userName, resetLink);
    }
}
