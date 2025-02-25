package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.NotificationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Notification;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.NotificationRepository;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.NotificationService;
import com.ntt.ntt.Service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final QnaService qnaService;
    private final NotificationRepository notificationRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);



    @GetMapping("/unreadCount")
    public ResponseEntity<Integer> getUnreadCount() {
        try {
            int unreadCount = notificationRepository.countByIsReadFalse();
            logger.debug("Unread notifications count: {}", unreadCount);
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            logger.error("Error fetching unread count", e);
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ 관리자 알림 목록 조회 (GET 요청 허용)
    @GetMapping("/admin")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForAdmin() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications(); // DTO 변환 후 반환
        return ResponseEntity.ok(notifications);
    }

    // ✅ 댓글 알림 목록 가져오기
    @GetMapping("/replies")
    public ResponseEntity<?> getReplyNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Member member = memberRepository.findByMemberEmail(userDetails.getUsername()).orElse(null);

        if (member == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자를 찾을 수 없습니다.");
        }

        List<Notification> notifications = notificationRepository.findByMemberAndIsReadFalseAndNotificationMessageContaining(member, "댓글");

        // DTO 변환 추가
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(notificationDTOs);
    }


    // ✅ 읽지 않은 댓글 알림 수 가져오기
    @GetMapping("/replies/unreadCount")
    public ResponseEntity<Long> getUnreadReplyNotificationCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0L);
        }

        long unreadCount = notificationService.getUnreadReplyNotificationCountForMember(userDetails.getUsername());
        return ResponseEntity.ok(unreadCount);
    }


    // 🔹 로그인한 사용자의 알림 목록 조회
    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림을 가져옵니다.")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationDTO> notifications = notificationService.getNotificationsForMember(userDetails.getUsername());
        System.out.println("📢 사용자 알림 개수: " + notifications.size()); // 디버깅 로그 추가
        return ResponseEntity.ok(notifications);
    }

    // 🔹 알림 읽음 처리
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }


    // 🔹 알림 삭제
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer id) {
        try {
            notificationService.deleteNotification(id);  // 알림 삭제
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("알림 삭제 중 오류 발생", e);
            return ResponseEntity.status(500).build();
        }
    }


}