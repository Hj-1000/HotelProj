package com.ntt.ntt.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async  // 비동기 이메일 전송
    public void sendPasswordResetEmail(String toEmail, String resetCode, String memberName) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("jung04251@gmail.com", "NTT호텔"); // 보내는 사람 이름 설정
            helper.setTo(toEmail);
            helper.setSubject("NTT 호텔 비밀번호 재설정 인증 코드"); // 이메일 제목

            // 이메일 내용
            String htmlContent = "<html><body>"
                    + "<div style='width: 100%; max-width: 540px; padding: 20px; border-radius: 10px; border: 1px solid #ddd;'>"
                    + "<div style='display: flex; align-items: center;'>"
                    + "<img src='cid:resetImage' alt='Reset Image' style='width: 100px; height: 100px; margin-right: 10px;' />"
                    + "<h2 style='font-family: Arial, sans-serif;'>NTT호텔 비밀번호 인증 코드 이메일입니다.</h2>"
                    + "</div>"
                    + "<hr style='border: none; height: 1px; background-color: #ddd; margin: 10px 0;'>"
                    + "<p style='font-size: 16px; padding-left: 110px;'><br></p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'>안녕하세요 <strong style='font-size: 18px;'>" + memberName + "</strong> 님.</p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'>요청하신 인증 코드는 <strong style='font-size: 24px; color: #FF5722;'>" + resetCode + "</strong> 입니다."
                    + "<p style='font-size: 16px; padding-left: 110px;'><br></p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'><br></p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'>코드를 정확하게 입력해주세요.</p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'>감사합니다, NTT호텔</p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'><br></p>"
                    + "<p style='font-size: 14px; padding-left: 110px;'>*인증 코드를 요청한 적 없다면 관리자에게 문의해주세요.</p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'><br></p>"
                    + "</div>"
                    + "</body></html>";

            helper.setText(htmlContent, true);

            // classpath에서 이미지 파일 로드
            ClassPathResource imageResource = new ClassPathResource("static/img/logo.jpg");
            helper.addInline("resetImage", imageResource);

            emailSender.send(message);  // 이메일 전송

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace(); // 로그
        }
    }
}