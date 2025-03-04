package com.ntt.ntt.Repository;


import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Reservation;
import com.ntt.ntt.Entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    // 특정 방의 첫 번째 예약 정보 가져오기 (Optional 사용)
    Optional<Reservation> findFirstByRoom_RoomId(Integer roomId);

    // 특정 방의 모든 예약 정보 가져오기 (필요 시 사용)
    List<Reservation> findAllByRoom_RoomId(Integer roomId);

    // 특정 회원의 모든 예약 정보 가져오기
    @Query("SELECT r FROM Reservation r WHERE r.member.memberId = :memberId")
    Page<Reservation> findAllByMember_MemberId(Integer memberId, Pageable pageable);

    // 특정회원의 이메일로 예약 정보 가져오기
    List<Reservation> findByMember_MemberEmail(String memberEmail);

    // 특정 방이 예약되었는지 여부 확인
    boolean existsByRoom_RoomId(Integer roomId);

    // 특정 방에 '예약' 상태의 예약이 존재하는지 확인하는 메서드 추가
    boolean existsByRoom_RoomIdAndReservationStatus(Integer roomId, String reservationStatus);

    // 특정 방의 예약을 삭제 (예약 취소 기능)
    void deleteByRoom_RoomId(Integer roomId);

    // 특정 방이 예약되었는지 확인 (새로운 예약과 겹치는 경우)
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.room.roomId = :roomId " +
            "AND (r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate) " +
            "AND (:excludeReservationId IS NULL OR r.reservationId <> :excludeReservationId)")
    boolean isRoomAlreadyBooked(@Param("roomId") Integer roomId,
                                @Param("checkInDate") LocalDateTime checkInDate,
                                @Param("checkOutDate") LocalDateTime checkOutDate,
                                @Param("excludeReservationId") Integer excludeReservationId);

    // 페이징 처리
    Page<Reservation> findAll(Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.reservationStatus <> '취소 완료'")
    Page<Reservation> findNonCancelledReservations(Pageable pageable);

    // 객실 이름으로 검색
    Page<Reservation> findByRoom_RoomNameContaining(String roomName, Pageable pageable);

    // 예약자 ID로 검색
    Page<Reservation> findByMember_MemberId(Integer memberId, Pageable pageable);

    // 예약자 이름으로 검색
    Page<Reservation> findByMember_MemberNameContaining(String memberName, Pageable pageable);

    // 취소 완료된 예약 삭제
    @Query("SELECT r FROM Reservation r WHERE r.reservationStatus = '취소 완료' AND r.cancelConfirmedAt <= :threshold")
    List<Reservation> findReservationsForDeletion(@Param("threshold") LocalDateTime threshold);

    // 결제 정보 조회
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.payments WHERE r.member.memberId = :memberId")
    Page<Reservation> findAllByMember_MemberIdWithPayments(Integer memberId, Pageable pageable);

    // 특정 회원이 특정 객실을 예약한 적이 있는지 확인
    boolean existsByMemberAndRoom(Member member, Room room);
}
