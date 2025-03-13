package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.NotificationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Notification;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.NotificationRepository;
import com.ntt.ntt.Service.NotificationService;
import groovy.util.logging.Log4j2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "NotificationController", description = "Q&A , 댓글 알림페이지")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Operation(summary = "읽지 않은 알림 수 조회", description = "읽지 않은 알림의 개수를 반환합니다.")
    @GetMapping("/unreadCount")
    public ResponseEntity<Integer> getUnreadCountForm() {
        try {
            int unreadCount = notificationRepository.countByIsReadFalse();
            logger.debug("Unread notifications count: {}", unreadCount);
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            logger.error("Error fetching unread count", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "관리자 알림 목록 조회", description = "관리자가 모든 알림을 조회합니다.")
    @GetMapping("/admin")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForAdminForm() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "댓글 알림 목록 조회", description = "사용자에게 해당하는 댓글 알림을 조회합니다.")
    @GetMapping("/replies")
    public ResponseEntity<?> getReplyNotificationsForm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Member member = memberRepository.findByMemberEmail(userDetails.getUsername()).orElse(null);

        if (member == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자를 찾을 수 없습니다.");
        }

        List<Notification> notifications = notificationRepository.findByMemberAndIsReadFalseAndNotificationMessageContaining(member, "댓글");

        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(notificationDTOs);
    }

    @Operation(summary = "읽지 않은 댓글 알림 수 조회", description = "읽지 않은 댓글 알림의 개수를 반환합니다.")
    @GetMapping("/replies/unreadCount")
    public ResponseEntity<Long> getUnreadReplyNotificationCountForm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0L);
        }

        long unreadCount = notificationService.getUnreadReplyNotificationCountForMember(userDetails.getUsername());
        return ResponseEntity.ok(unreadCount);
    }

    @Operation(summary = "사용자 알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotificationsForm(@AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationDTO> notifications = notificationService.getNotificationsForMember(userDetails.getUsername());
        System.out.println("📢 사용자 알림 개수: " + notifications.size()); // 디버깅 로그 추가
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

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