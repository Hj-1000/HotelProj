package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Payment;
import com.ntt.ntt.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findByReservation(Reservation reservation);
    List<Payment> findByRoomRoomIdIn(List<Integer> roomIds);

    void deleteByMember(Member member);
}
