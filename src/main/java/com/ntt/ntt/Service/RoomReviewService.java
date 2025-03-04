package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.RoomReviewDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.RoomReview;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Repository.RoomReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class RoomReviewService {

    private final RoomReviewRepository roomReviewRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    // 1. 리뷰 등록
    public RoomReviewDTO registerReview(RoomReviewDTO reviewDTO) {
        log.info("리뷰 등록 요청: {}", reviewDTO);

        Room room = roomRepository.findById(reviewDTO.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("해당 객실이 존재하지 않습니다."));
        Member member = memberRepository.findById(reviewDTO.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 리뷰 등록 전 예약 여부 확인
        boolean hasReservation = reservationRepository.existsByMemberAndRoom(member, room);
        if (!hasReservation) {
            throw new IllegalArgumentException("객실을 예약한 사용자만 리뷰를 작성할 수 있습니다.");
        }

        // 리뷰 저장
        RoomReview roomReview = RoomReview.builder()
                .rating(reviewDTO.getRating())
                .reviewText(reviewDTO.getReviewText())
                .room(room)
                .member(member)
                .build();

        RoomReview savedReview = roomReviewRepository.save(roomReview);
        log.info(" 리뷰 저장 완료: {}", savedReview);

        return RoomReviewDTO.fromEntity(savedReview);
    }

    // 2. 특정 리뷰 조회 (읽기 전용)
    @Transactional(readOnly = true)
    public RoomReviewDTO getReviewById(Integer reviewId) {
        log.info("리뷰 조회 요청: reviewId={}", reviewId);

        RoomReview roomReview = roomReviewRepository.findById(reviewId)
                .orElse(null); // 존재하지 않으면 null 반환

        if (roomReview == null) {
            log.warn("리뷰 조회 실패: reviewId={} - 해당 리뷰가 존재하지 않습니다.", reviewId);
            return null; // 예외를 던지지 않고 null 반환
        }

        return RoomReviewDTO.fromEntity(roomReview);
    }


    // 3. 특정 객실의 모든 리뷰 조회 (읽기 전용)
    @Transactional(readOnly = true)
    public List<RoomReviewDTO> getReviewsByRoomId(Integer roomId) {
        log.info("객실 리뷰 목록 조회: roomId={}", roomId);

        List<RoomReview> reviews = roomReviewRepository.findAllByRoom_RoomId(roomId);

        if (reviews == null || reviews.isEmpty()) {
            return Collections.emptyList(); // Null 방지
        }

        return reviews.stream()
                .map(RoomReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }


    // 4. 특정 객실의 최근 3개 리뷰 조회 (읽기 전용)
    @Transactional(readOnly = true)
    public List<RoomReviewDTO> getRecentReviewsByRoomId(Integer roomId) {
        log.info("객실 최근 리뷰 조회: roomId={}", roomId);
        return roomReviewRepository.findTop3ByRoom_RoomIdOrderByReviewDateDesc(roomId)
                .stream().map(RoomReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 5. 특정 회원이 작성한 리뷰 조회 (읽기 전용)
    @Transactional(readOnly = true)
    public List<RoomReviewDTO> getReviewsByMemberId(Integer memberId) {
        log.info("회원 리뷰 목록 조회: memberId={}", memberId);
        return roomReviewRepository.findAllByMember_MemberId(memberId)
                .stream().map(RoomReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 6. 객실 평균 평점 조회 (읽기 전용)
    @Transactional(readOnly = true)
    public Double getAverageRatingByRoomId(Integer roomId) {
        log.info("객실 평균 평점 조회: roomId={}", roomId);
        return roomReviewRepository.findAverageRatingByRoom_RoomId(roomId);
    }

    // 7. 특정 리뷰 수정
    public RoomReviewDTO updateReview(Integer reviewId, RoomReviewDTO reviewDTO) {
        log.info("리뷰 수정 요청: reviewId={}, reviewDTO={}", reviewId, reviewDTO);

        RoomReview existingReview = roomReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(" 해당 리뷰가 존재하지 않습니다. reviewId: " + reviewId));

        // 새로운 객체로 업데이트
        RoomReview updatedReview = RoomReview.builder()
                .reviewId(existingReview.getReviewId())
                .room(existingReview.getRoom())
                .member(existingReview.getMember())
                .rating(reviewDTO.getRating())
                .reviewText(reviewDTO.getReviewText())
                .build();

        RoomReview savedReview = roomReviewRepository.save(updatedReview);
        log.info(" 리뷰 수정 완료: {}", savedReview);

        return RoomReviewDTO.fromEntity(savedReview);
    }

    // 8. 특정 리뷰 삭제
    public void deleteReview(Integer reviewId) {
        log.info("리뷰 삭제 요청: reviewId={}", reviewId);
        RoomReview roomReview = roomReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(" 해당 리뷰가 존재하지 않습니다. reviewId: " + reviewId));

        roomReviewRepository.delete(roomReview);
        log.info(" 리뷰 삭제 완료: reviewId={}", reviewId);
    }
}
