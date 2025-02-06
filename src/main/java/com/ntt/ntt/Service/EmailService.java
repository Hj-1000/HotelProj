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
            helper.setText("비밀번호 찾기 코드: " + resetCode, false); // 이메일 내용

            emailSender.send(message);  // 이메일 전송

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace(); // 로그 출력 (실제 서비스에서는 로깅 처리 필요)
        }
    }
}