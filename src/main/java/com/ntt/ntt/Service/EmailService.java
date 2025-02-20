package com.ntt.ntt.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async  // 비동기 이메일 전송
    public void sendPasswordResetEmail(String toEmail, String resetCode) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("jung04251@gmail.com", "NTT호텔"); // 보내는 사람 이름 설정
            helper.setTo(toEmail);
            helper.setSubject("비밀번호 찾기 코드"); // 이메일 제목
            helper.setText("<html><body>"
                    + "<div style='width: 100%; max-width: 410px; padding: 20px; border-radius: 10px; border: 1px solid #ddd; box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);'>"
                    + "<h3 style='color: #4CAF50; font-family: Arial, sans-serif;'>비밀번호 찾기 코드</h3>"
                    + "<p style='font-size: 16px;'>비밀번호 찾기 코드: <strong style='font-size: 18px; color: #FF5722;'>" + resetCode + "</strong></p>"
                    + "<p style='font-size: 14px; color: #555;'>코드를 정확하게 입력해주세요.</p>"
                    + "<p style='font-size: 14px; color: #888;'>감사합니다, NTT호텔</p>"
                    + "</div></div>"
                    + "</body></html>", true); // 이메일 내용

            emailSender.send(message);  // 이메일 전송

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace(); // 로그
        }
    }
}