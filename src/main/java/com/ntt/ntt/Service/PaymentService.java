package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.PaymentDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Payment;
import com.ntt.ntt.Entity.Reservation;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.PaymentRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    // 결제 정보 저장 메소드
    public Payment savePayment(PaymentDTO paymentDTO) {
        // 해당 ID로 Member, Room, Reservation 엔티티를 조회
        Member member = memberRepository.findById(paymentDTO.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Room room = roomRepository.findById(paymentDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Reservation reservation = reservationRepository.findById(paymentDTO.getReservationId())
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // 기존 결제 정보 조회 (reservationId로 조회)
        Payment existingPayment = paymentRepository.findByReservation(reservation);

        if (existingPayment != null) {
            // 기존 결제 정보가 있으면 새로운 값을 기존 값에 더함
            existingPayment.setRoomPrice(paymentDTO.getRoomPrice());
            existingPayment.setRoomServicePrice(existingPayment.getRoomServicePrice() + paymentDTO.getRoomServicePrice()); // 기존값 + 새로운값
            existingPayment.setTotalPrice(paymentDTO.getTotalPrice());
            existingPayment.setModDate(LocalDateTime.now());

            // 업데이트 후 저장
            paymentRepository.save(existingPayment);
            return existingPayment;
        } else {
            // 기존 결제 정보가 없으면 새로 결제 정보 저장
            Payment payment = new Payment();
            payment.setRoomPrice(paymentDTO.getRoomPrice());
            payment.setRoomServicePrice(paymentDTO.getRoomServicePrice());
            payment.setTotalPrice(paymentDTO.getTotalPrice());
            payment.setMember(member);
            payment.setRoom(room);
            payment.setReservation(reservation);
            payment.setRegDate(LocalDateTime.now());
            payment.setModDate(LocalDateTime.now());

            // 새로운 결제 정보 저장
            paymentRepository.save(payment);
            return payment;
        }
    }
}

