package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Notification;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Repository.QnaRepository;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private Integer notificationId;
    private String notificationMessage;
    private boolean isRead;
    private LocalDateTime timestamp;
    private Integer qnaId;

    // Notification 엔티티를 DTO로 변환하는 메서드
    public static NotificationDTO fromEntity(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setNotificationMessage(notification.getNotificationMessage());
        dto.setRead(notification.isRead());
        dto.setTimestamp(notification.getTimestamp());

        // Qna 객체가 null이 아니면 qnaId 설정
        if (notification.getQna() != null) {
            dto.setQnaId(notification.getQna().getQnaId());
        } else {
            dto.setQnaId(null); // 명시적으로 설정
        }

        return dto;
    }
}