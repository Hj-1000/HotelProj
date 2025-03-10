package com.ntt.ntt.Controller.RoomReview;

import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.DTO.RoomReviewDTO;
import com.ntt.ntt.Service.RoomReviewService;
import com.ntt.ntt.Service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "RoomReviewController", description = "유저 리뷰 관리 컨트롤러")
public class RoomReviewController {

    private final RoomReviewService roomReviewService;
    private final RoomService roomService;

    /* -----------유저 페이지----------- */

    @Operation(summary = "리뷰 등록", description = "유저가 특정 객실에 대한 리뷰를 등록한다.")
    @PostMapping("/register")
    public ResponseEntity<?> registerReviewProc(@RequestBody RoomReviewDTO reviewDTO) {
        log.info("리뷰 등록 요청: {}", reviewDTO);
        try {
            RoomReviewDTO savedReview = roomReviewService.registerReview(reviewDTO);

            //  memberName을 포함한 JSON 반환
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reviewId", savedReview.getReviewId(),
                    "roomId", savedReview.getRoomId(),
                    "memberId", savedReview.getMemberId(),
                    "memberName", savedReview.getMemberName(),
                    "rating", savedReview.getRating(),
                    "reviewText", savedReview.getReviewText(),
                    "reviewDate", savedReview.getReviewDate()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @Operation(summary = "리뷰 조회", description = "리뷰 ID를 기반으로 특정 리뷰를 조회한다.")
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewByIdForm(@PathVariable Integer reviewId) {
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

    @Operation(summary = "객실 리뷰 조회", description = "특정 객실의 모든 리뷰를 페이징 처리하여 조회한다.")
    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getReviewsByRoomIdForm(
            @PathVariable Integer roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("객실 리뷰 조회 요청: roomId={}, page={}, size={}", roomId, page, size);
        Page<RoomReviewDTO> reviewPage = roomReviewService.getReviewsByRoomId(roomId, page, size);
        return ResponseEntity.ok(reviewPage);
    }

    @Operation(summary = "최근 객실 리뷰 조회", description = "특정 객실의 최근 3개 리뷰를 조회한다.")
    @GetMapping("/room/{roomId}/recent")
    public ResponseEntity<List<RoomReviewDTO>> getRecentReviewsByRoomIdForm(@PathVariable Integer roomId) {
        log.info("객실 최근 3개 리뷰 조회 요청: roomId={}", roomId);
        List<RoomReviewDTO> reviews = roomReviewService.getRecentReviewsByRoomId(roomId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "회원 리뷰 조회", description = "특정 회원이 작성한 모든 리뷰를 조회한다.")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<RoomReviewDTO>> getReviewsByMemberIdForm(@PathVariable Integer memberId) {
        log.info("회원 리뷰 조회 요청: memberId={}", memberId);
        List<RoomReviewDTO> reviews = roomReviewService.getReviewsByMemberId(memberId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "객실 평균 평점 조회", description = "특정 객실의 평균 평점을 조회한다.")
    @GetMapping("/room/{roomId}/rating")
    public ResponseEntity<Double> getAverageRatingByRoomIdForm(@PathVariable Integer roomId) {
        log.info("객실 평균 평점 조회 요청: roomId={}", roomId);
        Double averageRating = roomReviewService.getAverageRatingByRoomId(roomId);
        return ResponseEntity.ok(averageRating);
    }

    @Operation(summary = "리뷰 수정", description = "유저가 자신이 작성한 리뷰를 수정한다.")
    @PutMapping("/update/{reviewId}")
    @PreAuthorize("@roomReviewService.isReviewOwnerProc(#reviewId, authentication.principal.username)")
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

    @Operation(summary = "리뷰 삭제", description = "유저가 자신이 작성한 리뷰를 삭제한다.")
    @PostMapping("/delete/{reviewId}")
    @PreAuthorize("@roomReviewService.isReviewOwnerProc(#reviewId, authentication.principal.username)")
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

    @Operation(summary = "객실 상세보기", description = "객실 상세보기 페이지를 반환하며, 해당 객실의 리뷰 목록도 함께 제공한다.")
    @GetMapping("/room/detail/{roomId}")
    public String getRoomDetailForm(@PathVariable Integer roomId, Model model) {
        log.info("🔍 객실 상세보기 요청 - roomId: {}", roomId);

        RoomDTO room = roomService.readRoom(roomId);
        Page<RoomReviewDTO> reviewPage = roomReviewService.getReviewsByRoomId(roomId, 0, 10);

        log.info(" 리뷰 개수 (Controller): {}", reviewPage.getTotalElements());

        // Thymeleaf에서 리스트로 인식하도록 명확하게 변환
        List<RoomReviewDTO> reviews = new ArrayList<>(reviewPage.getContent());

        log.info(" 변환된 reviews 리스트 크기: {}", reviews.size());
        reviews.forEach(review -> log.info(" 리뷰 정보: ID={}, 내용={}, 작성자={}",
                review.getReviewId(),
                review.getReviewText(),
                review.getMemberName()));

        model.addAttribute("room", room);
        model.addAttribute("reviews", reviews);
        model.addAttribute("totalReviews", reviewPage.getTotalElements()); // 전체 리뷰 개수 추가

        return "detail";
    }

    @Operation(summary = "호텔 리뷰 조회", description = "특정 호텔의 모든 리뷰를 조회한다.")
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<?> getReviewsByHotelIdForm( @PathVariable Integer hotelId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {

        log.info("객실 리뷰 조회 요청: roomId={}, page={}, size={}", hotelId, page, size);
        Page<RoomReviewDTO> reviewPage = roomReviewService.getReviewsByHotelId(hotelId, page, size);
        return ResponseEntity.ok(reviewPage);
    }
}
