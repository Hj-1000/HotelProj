package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.NotificationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Notification;
import com.ntt.ntt.Service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String getNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            // 관리자 권한 확인
            if (userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {

                // 알림 목록 조회
                List<Notification> notifications = notificationService.getNotificationsForAdmin();
                model.addAttribute("notifications", notifications);
            }
        }
        return "admin/executive"; // 관리자가 볼 수 있는 페이지
    }

    // 🔹 로그인한 사용자의 알림 목록 조회
    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림을 가져옵니다.")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@AuthenticationPrincipal Member member) {
        List<NotificationDTO> notifications = notificationService.getNotificationsForMember(String.valueOf(member.getMemberId()));
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
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }
}
