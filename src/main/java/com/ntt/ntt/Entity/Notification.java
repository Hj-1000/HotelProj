package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId; // 알림 ID

    private String notificationMessage; // 알림 내용

    private boolean isRead = false; // 읽음 여부 (기본값: false)

    private LocalDateTime timestamp = LocalDateTime.now(); // 생성 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member; // 알림을 받을 사용자
}