package com.ntt.ntt.Repository;


import com.ntt.ntt.Entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    // 특정 방이 예약되었는지 여부 확인
    boolean existsByRoom_RoomId(Integer roomId);

    // 특정 방의 예약을 삭제 (예약 취소 기능)
    void deleteByRoom_RoomId(Integer roomId);

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

}
