package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.NotificationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Notification;
import com.ntt.ntt.Entity.Qna;
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
        Member admin = memberRepository.findByRole(Role.valueOf("ADMIN"))
                .orElseThrow(() -> new RuntimeException("관리자 계정을 찾을 수 없습니다."));
        return notificationRepository.findAllByOrderByTimestampDesc();
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



    // 알림 삭제
    @Transactional
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // 새로운 알림 생성
    public void createNotification(Member member, String message, Qna qna) {
        // ✅ 기존에 같은 메시지와 Qna가 있는지 확인 (사용자 기준)
        boolean exists = notificationRepository.existsByNotificationMessageAndQna(message, qna);

        if (!exists) { // 중복이 없을 경우에만 저장
            Notification notification = new Notification();
            notification.setNotificationMessage(message);
            notification.setMember(member); // 원래 글 작성자
            notification.setRead(false);
            notification.setTimestamp(LocalDateTime.now());
            notification.setQna(qna);

            notificationRepository.save(notification);
            System.out.println("🔔 알림 생성됨: " + message);
        }
    }



    // 알림 읽음 처리
    @Transactional
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        notification.setRead(true);  // 읽음 상태로 변경
        notificationRepository.save(notification);  // DB에 반영
    }


    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }


}