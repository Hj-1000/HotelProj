package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.NotificationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Notification;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.NotificationRepository;
import groovy.util.logging.Log4j2;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    // 관리자가 볼 수 있는 알림 목록을 가져오는 메소드
    public List<Notification> getNotificationsForAdmin() {
        return notificationRepository.findByIsReadFalse(); // 읽지 않은 알림들
    }

    // 특정 사용자의 알림 목록 조회
    public List<NotificationDTO> getNotificationsForMember(String memberEmail) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return notificationRepository.findByMemberOrderByTimestampDesc(member)
                .stream()
                .map(NotificationDTO::fromEntity) // fromEntity 메서드를 사용하도록 수정
                .collect(Collectors.toList());
    }

    // 알림 읽음 처리
    @Transactional
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    // 알림 삭제
    @Transactional
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // 새로운 알림 생성
    public void createNotification(Member member, String message) {
        Notification notification = new Notification();
        notification.setNotificationMessage(message);
        notification.setMember(member);  // 알림을 받을 멤버 (이 경우 관리자)
        notification.setIsRead(false);   // 알림은 기본적으로 읽지 않은 상태
        notification.setTimestamp(LocalDateTime.now());

        notificationRepository.save(notification);
        System.out.println("🔔 알림 생성됨: " + message);
    }

    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }
}