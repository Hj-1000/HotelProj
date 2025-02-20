package com.ntt.ntt.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
            helper.setSubject("NTT 호텔 비밀번호 인증 코드"); // 이메일 제목

            // 이메일 내용
            String htmlContent = "<html><body>"
                    + "<div style='width: 100%; max-width: 520px; padding: 20px; border-radius: 10px; border: 1px solid #ddd;'>"
                    + "<div style='display: flex; align-items: center;'>"
                    + "<img src='cid:resetImage' alt='Reset Image' style='width: 100px; height: 100px; margin-right: 10px;' />"
                    + "<h2 style='color: #4CAF50; font-family: Arial, sans-serif;'>NTT호텔 비밀번호 인증 코드 이메일입니다.</h2>"
                    + "</div><br>"
                    + "<p style='font-size: 16px; padding-left: 110px;'>안녕하세요 <strong style='font-size: 18px;'>" + memberName + "</strong> 님</p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'>요청하신 인증 코드는 <strong style='font-size: 24px; color: #FF5722;'>" + resetCode + "</strong> 입니다.</p><br><br>"
                    + "<p style='font-size: 16px; padding-left: 110px;'>코드를 정확하게 입력해주세요.</p>"
                    + "<p style='font-size: 16px; padding-left: 110px;'>감사합니다, NTT호텔</p><br>"
                    + "</div>"
                    + "</body></html>";

            helper.setText(htmlContent, true);

            // 이미지를 inline으로 첨부 (cid로 참조)
            File image = new File("C:\\Users\\woori\\IdeaProjects\\NTT\\src\\main\\resources\\static\\img\\logo.jpg");  // 이미지 파일 경로
            helper.addInline("resetImage", image);  // cid:resetImage로 참조

            emailSender.send(message);  // 이메일 전송

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace(); // 로그
        }
    }
}