package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Notification;
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
    private boolean isRead; // boolean 타입

    private LocalDateTime timestamp;

    // Notification 엔티티를 DTO로 변환하는 메서드
    public static NotificationDTO fromEntity(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setNotificationMessage(notification.getNotificationMessage());
        dto.setRead(notification.isRead());
        dto.setTimestamp(notification.getTimestamp());
        return dto;
    }

    // DTO를 Notification 엔티티로 변환하는 메서드
    public Notification toEntity() {
        Notification notification = new Notification();
        notification.setNotificationId(this.notificationId);
        notification.setNotificationMessage(this.notificationMessage);
        notification.setIsRead(this.isRead);
        notification.setTimestamp(this.timestamp);
        return notification;
    }
}