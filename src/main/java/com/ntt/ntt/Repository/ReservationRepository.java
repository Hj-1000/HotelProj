package com.ntt.ntt.Repository;


import com.ntt.ntt.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    // 특정 회원의 모든 예약 가져오기
    List<Reservation> findByMember_MemberId(Integer memberId);

    // 특정 방에 대한 예약 목록 가져오기
    List<Reservation> findByRoom_RoomId(Integer roomId);
}
