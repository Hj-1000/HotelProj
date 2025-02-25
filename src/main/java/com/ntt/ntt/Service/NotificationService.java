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



    // 특정 사용자의 알림 목록 조회
    public List<NotificationDTO> getNotificationsForMember(String memberEmail) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return notificationRepository.findByMemberOrderByTimestampDesc(member)
                .stream()
                .map(NotificationDTO::fromEntity) // fromEntity 메서드를 사용하도록 수정
                .collect(Collectors.toList());
    }

    // 댓글 등록 시 생성된 알림만 가져오기
    public List<NotificationDTO> getReplyNotificationsForMember(String memberEmail) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return notificationRepository.findByMemberOrderByTimestampDesc(member)
                .stream()
                .filter(n -> n.getNotificationMessage().contains("댓글")) // ✅ 댓글 관련 알림만 필터링
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }




    // 알림 삭제
    @Transactional
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // 새로운 알림 생성
    public void createNotification(Member member, String message, Qna qna) {
        boolean exists = notificationRepository.existsByNotificationMessageAndQnaAndMember(message, qna, member);

        if (!exists) { // 중복이 없을 경우에만 저장
            Notification notification = new Notification();
            notification.setNotificationMessage(message);
            notification.setMember(member); // 알림을 받을 사용자 (Q&A 작성자)
            notification.setRead(false);
            notification.setTimestamp(LocalDateTime.now());
            notification.setQna(qna);

            notificationRepository.save(notification);
            System.out.println("🔔 알림 생성됨: " + message);
        }
    }

    // ✅ 특정 사용자의 읽지 않은 댓글 알림 개수 조회
    public long getUnreadReplyNotificationCountForMember(String memberEmail) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return notificationRepository.findByMemberAndIsReadFalseAndNotificationMessageContaining(member, "댓글").size();
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