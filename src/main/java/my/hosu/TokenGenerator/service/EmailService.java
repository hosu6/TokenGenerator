package my.hosu.TokenGenerator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("TokenGenerator - 이메일 인증 번호");

            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; padding: 40px;'>" +
                    "  <div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);'>"
                    +
                    "    <div style='background: linear-gradient(135deg, #6366f1, #a855f7); padding: 30px; text-align: center; color: white;'>"
                    +
                    "      <h1 style='margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px;'>TokenGenerator</h1>"
                    +
                    "    </div>" +
                    "    <div style='padding: 40px; text-align: center; color: #1f2937;'>" +
                    "      <h2 style='margin-bottom: 24px; font-size: 20px;'>환영합니다!</h2>" +
                    "      <p style='margin-bottom: 32px; font-size: 16px; line-height: 1.5;'>회원가입을 완료하기 위해 아래의 6자리 인증 번호를 입력해 주세요.</p>"
                    +
                    "      <div style='font-size: 32px; font-weight: 800; color: #4f46e5; letter-spacing: 15px; text-align: center; margin: 30px 0;'> "
                    +
                    code +
                    " </div>" +
                    "      <p style='font-size: 12px; color: #6b7280; margin-bottom: 8px;'>인증 번호는 5분 동안 유효합니다.</p>" +
                    "      <p style='font-size: 12px; color: #9ca3af;'>본 메일은 발신 전용입니다.</p>" +
                    "    </div>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            log.error("Email sending failed to: {}", to, e);
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during email sending to: {}", to, e);
            throw new RuntimeException("이메일 발송 중 예상치 못한 오류 발생", e);
        }
    }
}
