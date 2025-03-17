package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.RoomReviewDTO;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.RoomReview;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Repository.RoomReviewRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    private final HotelRepository hotelRepository;
    private final MemberService memberService;

    // 리뷰 등록
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

        // 리뷰 저장 후 호텔 평점 업데이트 2025-03-05 추가
        Integer hotelId = room.getHotelId().getHotelId();  // 호텔 ID 가져오기
        hotelRepository.updateHotelRating(hotelId);
        log.info("호텔 평점 업데이트 완료: 호텔 ID = {}", hotelId);

        String maskedName = maskName(member.getMemberName());
        return RoomReviewDTO.fromEntity(savedReview, maskedName);
    }

    // 특정 리뷰 조회
    @Transactional(readOnly = true)
    public RoomReviewDTO getReviewById(Integer reviewId) {
        log.info("리뷰 조회 요청: reviewId={}", reviewId);

        RoomReview roomReview = roomReviewRepository.findById(reviewId)
                .orElse(null); // 존재하지 않으면 null 반환

        if (roomReview == null) {
            log.warn("리뷰 조회 실패: reviewId={} - 해당 리뷰가 존재하지 않습니다.", reviewId);
            return null; // 예외를 던지지 않고 null 반환
        }

        String maskedName = maskName(roomReview.getMember().getMemberName());
        return RoomReviewDTO.fromEntity(roomReview, maskedName);
    }

    // 특정 객실의 모든 리뷰 조회
    @Transactional(readOnly = true)
    public Page<RoomReviewDTO> getReviewsByRoomId(Integer roomId, int page, int size) {
        log.info(" getReviewsByRoomId 호출 - roomId: {}, page: {}, size: {}", roomId, page, size);

        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reviewDate"));

            // 리뷰 데이터 조회
            Page<RoomReview> reviews = roomReviewRepository.findByRoom_RoomIdOrderByReviewDateDesc(roomId, pageRequest);

            log.info(" 리뷰 조회 결과 개수 = {}", reviews.getTotalElements());

            // 결과가 null이면 빈 페이지 반환
            return Optional.ofNullable(reviews)
                    .orElse(Page.empty(pageRequest))
                    .map(roomReview -> {
                        String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                        return RoomReviewDTO.fromEntity(roomReview, maskedName);
                    });
        } catch (Exception e) {
            log.error(" 리뷰 조회 중 예외 발생 - roomId: {}, message: {}", roomId, e.getMessage());
            return Page.empty();
        }
    }

    // 모든 리뷰 조회
    public Page<RoomReviewDTO> getAllReviews(Pageable pageable, Authentication authentication) {
        // 로그인된 사용자의 memberId 가져오기
        Integer memberId = getLoggedInMemberId(authentication);

        // 로그인된 사용자 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // Enum을 사용하여 역할 가져오기
        Role role = member.getRole();

        switch (role) {
            case ADMIN:
                return roomReviewRepository.findAll(pageable).map(roomReview -> {
                    String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                    return RoomReviewDTO.fromEntity(roomReview, maskedName);
                });

            case CHIEF:
                //  CHIEF가 본사 소속 호텔을 관리할 때, `Company` 기준으로 hotelId 가져오기
                List<Integer> chiefCompanyHotels = hotelRepository.findByCompanyByMemberByMemberId(memberId)
                        .stream().map(Hotel::getHotelId)
                        .collect(Collectors.toList());

                log.info(" CHIEF의 본사 소속 호텔 목록: {}", chiefCompanyHotels);

                if (chiefCompanyHotels.isEmpty()) {
                    log.warn(" CHIEF가 관리하는 호텔이 없습니다! 리뷰 없음.");
                    return Page.empty(pageable);
                }

                return roomReviewRepository.findByHotelIdsOrderByReviewDateDesc(chiefCompanyHotels, pageable)
                        .map(roomReview -> {
                            String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                            return RoomReviewDTO.fromEntity(roomReview, maskedName);
                        });

            case MANAGER:
                List<Integer> managerHotelIds = hotelRepository.findHotelIdsByMemberId(memberId);
                log.info(" MANAGER의 호텔 목록: {}", managerHotelIds);

                if (managerHotelIds.isEmpty()) {
                    log.warn(" MANAGER가 관리하는 호텔이 없습니다! 리뷰 없음.");
                    return Page.empty(pageable);
                }

                return roomReviewRepository.findByHotelIdOrderByReviewDateDesc(managerHotelIds.get(0), pageable)
                        .map(roomReview -> {
                            String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                            return RoomReviewDTO.fromEntity(roomReview, maskedName);
                        });

            default:
                return Page.empty(pageable);
        }
    }

    // 특정 객실의 최근 3개 리뷰 조회
    @Transactional(readOnly = true)
    public List<RoomReviewDTO> getRecentReviewsByRoomId(Integer roomId) {
        log.info("객실 최근 리뷰 조회: roomId={}", roomId);
        return roomReviewRepository.findTop3ByRoom_RoomIdOrderByReviewDateDesc(roomId)
                .stream().map(roomReview -> {
                    String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                    return RoomReviewDTO.fromEntity(roomReview, maskedName);
                })
                .collect(Collectors.toList());
    }

    // 특정 회원이 작성한 리뷰 조회
    @Transactional(readOnly = true)
    public List<RoomReviewDTO> getReviewsByMemberId(Integer memberId) {
        log.info("회원 리뷰 목록 조회: memberId={}", memberId);
        return roomReviewRepository.findAllByMember_MemberId(memberId)
                .stream().map(roomReview -> {
                    String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                    return RoomReviewDTO.fromEntity(roomReview, maskedName);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RoomReviewDTO> searchReviews(String category, String keyword, Pageable pageable) {
        log.info("🔍 리뷰 검색 요청 - category: {}, keyword: {}", category, keyword);

        // 검색어가 없으면 전체 조회 반환
        if (category == null || keyword == null || keyword.isEmpty()) {
            return roomReviewRepository.findAll(pageable).map(roomReview -> {
                String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                return RoomReviewDTO.fromEntity(roomReview, maskedName);
            });
        }

        // 카테고리에 따라 검색
        switch (category) {
            case "reviewId":
                try {
                    Integer reviewId = Integer.parseInt(keyword);
                    return roomReviewRepository.findByReviewId(reviewId, pageable).map(roomReview -> {
                        String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                        return RoomReviewDTO.fromEntity(roomReview, maskedName);
                    });
                } catch (NumberFormatException e) {
                    log.warn(" 리뷰 ID 검색 실패 - 숫자 변환 오류: {}", keyword);
                    return Page.empty();
                }
            case "memberId":
                try {
                    Integer memberId = Integer.parseInt(keyword);
                    return roomReviewRepository.findByMember_MemberId(memberId, pageable).map(roomReview -> {
                        String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                        return RoomReviewDTO.fromEntity(roomReview, maskedName);
                    });
                } catch (NumberFormatException e) {
                    log.warn(" 회원 ID 검색 실패 - 숫자 변환 오류: {}", keyword);
                    return Page.empty();
                }
            case "memberName":
                return roomReviewRepository.findByMember_MemberNameContainingIgnoreCase(keyword, pageable)
                        .map(roomReview -> {
                            String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                            return RoomReviewDTO.fromEntity(roomReview, maskedName);
                        });
            case "roomId":
                try {
                    Integer roomId = Integer.parseInt(keyword);
                    return roomReviewRepository.findByRoom_RoomIdOrderByReviewDateDesc(roomId, pageable)
                            .map(roomReview -> {
                                String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                                return RoomReviewDTO.fromEntity(roomReview, maskedName);
                            });
                } catch (NumberFormatException e) {
                    log.warn(" 객실 ID 검색 실패 - 숫자 변환 오류: {}", keyword);
                    return Page.empty();
                }
            default:
                log.warn(" 잘못된 검색 카테고리: {}", category);
                return Page.empty();
        }
    }

    // 객실 평균 평점 조회
    @Transactional(readOnly = true)
    public Double getAverageRatingByRoomId(Integer roomId) {
        log.info("객실 평균 평점 조회: roomId={}", roomId);
        return roomReviewRepository.findAverageRatingByRoom_RoomId(roomId);
    }

    // 특정 리뷰 수정
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

        String maskedName = maskName(savedReview.getMember() != null ? savedReview.getMember().getMemberName() : "탈퇴한 회원");

        return RoomReviewDTO.fromEntity(savedReview, maskedName);
    }

    // 특정 리뷰 삭제
    public void deleteReview(Integer reviewId) {
        log.info("리뷰 삭제 요청: reviewId={}", reviewId);
        RoomReview roomReview = roomReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(" 해당 리뷰가 존재하지 않습니다. reviewId: " + reviewId));

        roomReviewRepository.delete(roomReview);
        log.info(" 리뷰 삭제 완료: reviewId={}", reviewId);
    }

    // 본인 리뷰인지 확인
    public boolean isReviewOwner(Integer reviewId, String username) {
        RoomReview review = roomReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));

        return review.getMember().getMemberEmail().equals(username);
    }


    // 호텔ID를 통하여 최신 리뷰 3개 가져오는 메서드
    @Transactional(readOnly = true)
    public List<RoomReviewDTO> getLatestReviewsByHotelId(Integer hotelId) {
        log.info("객실 최근 리뷰 조회: roomId={}", hotelId);
        Pageable pageable = PageRequest.of(0, 3);
        return roomReviewRepository.findTop3ByHotelIdOrderByReviewDateDesc(hotelId, pageable)
                .stream().map(roomReview -> {
                    String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                    return RoomReviewDTO.fromEntity(roomReview, maskedName);
                })
                .collect(Collectors.toList());
    }

    // 호텔ID를 통하여 최신 리뷰 전부 가져오는 메서드
    @Transactional(readOnly = true)
    public Page<RoomReviewDTO> getReviewsByHotelId(Integer hotelId, int page, int size) {
        log.info("DEBUG: getReviewsByHotelId 호출 - hotelId: {}, page: {}, size: {}", hotelId, page, size);

        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reviewDate"));

            // 리뷰 데이터 조회
            Page<RoomReview> reviews = roomReviewRepository.findByHotelIdOrderByReviewDateDesc(hotelId, pageRequest);

            log.info("DEBUG: 리뷰 조회 결과 개수 = {}", reviews.getTotalElements());

            // 결과가 null이면 빈 페이지 반환
            return Optional.ofNullable(reviews)
                    .orElse(Page.empty(pageRequest))
                    .map(roomReview -> {
                        String maskedName = maskName(roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원");
                        return RoomReviewDTO.fromEntity(roomReview, maskedName);
                    });
        } catch (Exception e) {
            log.error("ERROR: 리뷰 조회 중 예외 발생 - roomId: {}, message: {}", hotelId, e.getMessage());
            return Page.empty();
        }
    }

    // 이름 마스킹 메서드 (서비스에서 처리)
    private String maskName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "익명";
        }
        if (name.length() == 1) {
            return name;  // 길이가 1이면 그대로 반환
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";  // 길이가 2이면 첫 문자 + * 반환
        }
        // 길이가 3 이상이면, 첫 문자 + 중간 문자 *로 변환 + 마지막 문자
        return name.charAt(0) + "*".repeat(Math.max(0, name.length() - 2)) + name.charAt(name.length() - 1);
    }


    // 로그인한 유저의 memberId를 조회
    private Integer getLoggedInMemberId(Authentication authentication) {
        // authentication이 null이 아니고, 인증된 사용자가 있는지 확인
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("로그인된 사용자가 없습니다.");
        }
        // authentication.getName()을 memberName으로 대체
        String memberEmail = authentication.getName();

        // memberEmail이 null이거나 비어있을 경우 처리
        if (memberEmail == null || memberEmail.isEmpty()) {
            throw new RuntimeException("회원 정보가 존재하지 않습니다.");
        }

        // memberName을 통해 Member 조회
        Member member = memberService.findMemberByMemberEmail(memberEmail);

        // member가 null인 경우 처리
        if (member == null) {
            throw new RuntimeException("회원 정보가 존재하지 않습니다.");
        }

        return member.getMemberId(); // memberId 반환
    }
}
