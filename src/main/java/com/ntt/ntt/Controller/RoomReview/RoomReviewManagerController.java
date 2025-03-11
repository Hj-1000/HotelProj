package com.ntt.ntt.Controller.RoomReview;

import com.ntt.ntt.DTO.RoomReviewDTO;
import com.ntt.ntt.Service.RoomReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
@RequestMapping("/manager/room/reviews")
@RequiredArgsConstructor
@Log4j2
@PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
@Tag(name = "RoomReviewManagerController", description = "관리자 리뷰 관리 컨트롤러")
public class RoomReviewManagerController {

    private final RoomReviewService roomReviewService;

    /* -----------관리자 페이지----------- */

    @Operation(summary = "모든 리뷰 목록 조회", description = "관리자가 모든 객실 리뷰를 검색 조건과 함께 조회한다.")
    @GetMapping("/list")
    public String showReviewListForm(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        log.info(" 객실 리뷰 목록 요청 - category: {}, keyword: {}", category, keyword);

        // 검색 조건이 있을 경우 검색 수행
        Page<RoomReviewDTO> reviews;
        if (category != null && keyword != null && !keyword.trim().isEmpty()) {
            reviews = roomReviewService.searchReviews(category, keyword, Pageable.ofSize(size).withPage(page));
        } else {
            // 역할별로 필터링하여 리뷰 조회
            reviews = roomReviewService.getAllReviews(Pageable.ofSize(size).withPage(page), authentication);
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviews.getTotalPages());
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);

        return "manager/room/reviews/list";
    }

    @Operation(summary = "모든 리뷰 목록 데이터 반환", description = "관리자가 모든 객실 리뷰를 JSON 형식으로 조회한다.")
    @GetMapping("/list/data")
    public ResponseEntity<Page<RoomReviewDTO>> getReviewListDataForm(Pageable pageable,Authentication authentication) {
        log.info("🔍 객실 리뷰 목록 JSON 요청");

        // 로그인한 사용자의 역할(Role) 가져오기
        Page<RoomReviewDTO> reviews = roomReviewService.getAllReviews(pageable, authentication);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "특정 객실의 리뷰 조회", description = "관리자가 특정 객실의 모든 리뷰를 페이징하여 조회한다.")
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Page<RoomReviewDTO>> getReviewsByRoomIdForm(@PathVariable Integer roomId, Pageable pageable) {
        log.info(" 객실 리뷰 조회 요청 - roomId: {}", roomId);
        return ResponseEntity.ok(roomReviewService.getReviewsByRoomId(roomId, pageable.getPageNumber(), pageable.getPageSize()));
    }

    @Operation(summary = "특정 회원이 작성한 모든 리뷰 조회", description = "관리자가 특정 회원이 작성한 모든 리뷰를 조회한다.")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getReviewsByMemberIdForm(@PathVariable Integer memberId) {
        log.info("🔍 회원 리뷰 조회 요청 - memberId: {}", memberId);
        return ResponseEntity.ok(roomReviewService.getReviewsByMemberId(memberId));
    }

    @Operation(summary = "리뷰 수정", description = "관리자가 특정 리뷰를 수정한다.(관리자 권한이 필요)")
    @PutMapping("/update/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReviewProc(
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

    @Operation(summary = "리뷰 삭제", description = "관리자가 특정 리뷰를 삭제한다.(관리자 권한이 필요)")
    @GetMapping("/delete/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReviewForm(@PathVariable Integer reviewId) {
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
