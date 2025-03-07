package com.ntt.ntt.Controller.RoomReview;

import com.ntt.ntt.DTO.RoomReviewDTO;
import com.ntt.ntt.Service.RoomReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
@RequestMapping("/manager/room/reviews")
@RequiredArgsConstructor
@Log4j2
@PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
public class RoomReviewManagerController {

    private final RoomReviewService roomReviewService;

    // 1. 모든 리뷰 목록 조회
    @GetMapping("/list")
    public String showReviewList(Model model, @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        log.info(" 객실 리뷰 목록 페이지 요청 - 페이지: {}, 사이즈: {}", page, size);

        Page<RoomReviewDTO> reviews = roomReviewService.getAllReviews(Pageable.ofSize(size).withPage(page));

        model.addAttribute("reviews", reviews);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviews.getTotalPages());

        return "manager/room/reviews/list";
    }

    // 모든 리뷰 목록 반환
    @GetMapping("/list/data")
    public ResponseEntity<Page<RoomReviewDTO>> getReviewListData(Pageable pageable) {
        log.info("🔍 객실 리뷰 목록 JSON 요청");
        Page<RoomReviewDTO> reviews = roomReviewService.getAllReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    // 2. 특정 객실의 리뷰 조회
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Page<RoomReviewDTO>> getReviewsByRoomId(@PathVariable Integer roomId, Pageable pageable) {
        log.info(" 객실 리뷰 조회 요청 - roomId: {}", roomId);
        return ResponseEntity.ok(roomReviewService.getReviewsByRoomId(roomId, pageable.getPageNumber(), pageable.getPageSize()));
    }

    // 3. 특정 회원이 작성한 모든 리뷰 조회
    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getReviewsByMemberId(@PathVariable Integer memberId) {
        log.info("🔍 회원 리뷰 조회 요청 - memberId: {}", memberId);
        return ResponseEntity.ok(roomReviewService.getReviewsByMemberId(memberId));
    }

    // 4. 리뷰 수정
    @PutMapping("/update/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReview(
            @PathVariable Integer reviewId,
            @RequestBody RoomReviewDTO reviewDTO) {

        log.info("🔄 리뷰 수정 요청 (ADMIN) - reviewId: {}, reviewDTO: {}", reviewId, reviewDTO);

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

    // 5. 리뷰 삭제
    @GetMapping("/delete/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Integer reviewId) {
        log.info(" 리뷰 삭제 요청 (ADMIN) - reviewId: {}", reviewId);

        try {
            roomReviewService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of("success", true, "message", "리뷰가 삭제되었습니다."));
        } catch (Exception e) {
            log.error(" 리뷰 삭제 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

}
