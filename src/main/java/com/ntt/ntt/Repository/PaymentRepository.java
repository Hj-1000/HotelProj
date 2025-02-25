package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Payment;
import com.ntt.ntt.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findByReservation(Reservation reservation);
}
