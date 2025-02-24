package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.PaymentDTO;
import com.ntt.ntt.Entity.Payment;
import com.ntt.ntt.Repository.PaymentRepository;
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

    // 결제 정보 저장 메소드
    public Payment savePayment(PaymentDTO paymentDTO) {
        Payment payment = new Payment();
        payment.setRoomPrice(paymentDTO.getRoomPrice());
        payment.setRoomServicePrice(paymentDTO.getRoomServicePrice());
        payment.setRoomServiceName(paymentDTO.getRoomServiceName());
        payment.setMemberId(paymentDTO.getMemberId());
        payment.setRoomId(paymentDTO.getRoomId());
        payment.setReservationId(paymentDTO.getReservationId());
        payment.setRegDate(LocalDateTime.now());
        payment.setModDate(LocalDateTime.now());

        paymentRepository.save(payment);
        return payment;
    }
}

