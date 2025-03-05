package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.DTO.RoomReviewDTO;
import com.ntt.ntt.Service.RoomReviewService;
import com.ntt.ntt.Service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Log4j2
public class RoomReviewController {

    private final RoomReviewService roomReviewService;
    private final RoomService roomService;

    //  1. 리뷰 등록
    @PostMapping("/register")
    public ResponseEntity<?> registerReview(@RequestBody RoomReviewDTO reviewDTO) {
        log.info("리뷰 등록 요청: {}", reviewDTO);
        try {
            RoomReviewDTO savedReview = roomReviewService.registerReview(reviewDTO);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    //  2. 특정 리뷰 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Integer reviewId) {
        log.info("리뷰 조회 요청: reviewId={}", reviewId);

        try {
            RoomReviewDTO review = roomReviewService.getReviewById(reviewId);

            if (review == null) {
                log.warn("해당 리뷰가 존재하지 않음: reviewId={}", reviewId);
                return ResponseEntity.ok(Collections.emptyMap()); // 200 OK + 빈 객체 반환
            }

            return ResponseEntity.ok(review);
        } catch (Exception e) {
            log.error("리뷰 조회 중 오류 발생: reviewId={}, error={}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", "서버 오류가 발생했습니다."));
        }
    }

    //  3. 특정 객실의 모든 리뷰 조회
    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getReviewsByRoomId(@PathVariable Integer roomId) {
        log.info("객실 리뷰 조회 요청: roomId={}", roomId);
        try {
            List<RoomReviewDTO> reviews = roomReviewService.getReviewsByRoomId(roomId);

            if (reviews.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList()); // 빈 배열 반환
            }

            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("객실 리뷰 조회 중 오류 발생: roomId={}, error={}", roomId, e.getMessage());

            // `ResponseEntity<?>` 사용하여 타입 불일치 해결
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "리뷰를 불러오는 중 오류 발생"));
        }
    }

    //  4. 특정 객실의 최근 3개 리뷰 조회
    @GetMapping("/room/{roomId}/recent")
    public ResponseEntity<List<RoomReviewDTO>> getRecentReviewsByRoomId(@PathVariable Integer roomId) {
        log.info("객실 최근 3개 리뷰 조회 요청: roomId={}", roomId);
        List<RoomReviewDTO> reviews = roomReviewService.getRecentReviewsByRoomId(roomId);
        return ResponseEntity.ok(reviews);
    }

    //  5. 특정 회원이 작성한 리뷰 조회
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<RoomReviewDTO>> getReviewsByMemberId(@PathVariable Integer memberId) {
        log.info("회원 리뷰 조회 요청: memberId={}", memberId);
        List<RoomReviewDTO> reviews = roomReviewService.getReviewsByMemberId(memberId);
        return ResponseEntity.ok(reviews);
    }

    //  6. 객실 평균 평점 조회
    @GetMapping("/room/{roomId}/rating")
    public ResponseEntity<Double> getAverageRatingByRoomId(@PathVariable Integer roomId) {
        log.info("객실 평균 평점 조회 요청: roomId={}", roomId);
        Double averageRating = roomReviewService.getAverageRatingByRoomId(roomId);
        return ResponseEntity.ok(averageRating);
    }

    //  7. 리뷰 수정
    @PutMapping("/update/{reviewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER') or @roomReviewService.isReviewOwner(#reviewId, authentication.principal.username)")
    public ResponseEntity<?> updateReview(
            @PathVariable Integer reviewId,
            @RequestBody RoomReviewDTO reviewDTO) {
        log.info("리뷰 수정 요청: reviewId={}, reviewDTO={}", reviewId, reviewDTO);

        if (reviewId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "리뷰 ID가 누락되었습니다."));
        }

        try {
            RoomReviewDTO updatedReview = roomReviewService.updateReview(reviewId, reviewDTO);
            log.info(" 리뷰 수정 성공: {}", updatedReview);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error(" 리뷰 수정 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    //  8. 리뷰 삭제
    @PostMapping("/delete/{reviewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER') or @roomReviewService.isReviewOwner(#reviewId, authentication.principal.username)")
    public ResponseEntity<?> deleteReview(@PathVariable Integer reviewId) {
        log.info("리뷰 삭제 요청: reviewId={}", reviewId);

        try {
            roomReviewService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of("success", true, "message", "리뷰가 삭제되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    //  9. 객실 상세보기
    @GetMapping("/room/detail/{roomId}")
    public String getRoomDetail(@PathVariable Integer roomId, Model model) {
        log.info("객실 상세보기 요청 - Room ID: {}", roomId);

        RoomDTO room = roomService.readRoom(roomId);
        List<RoomReviewDTO> reviews = roomReviewService.getReviewsByRoomId(roomId);

        model.addAttribute("room", room);
        model.addAttribute("reviews", reviews);

        return "detail"; // Thymeleaf 템플릿 반환
    }
}
