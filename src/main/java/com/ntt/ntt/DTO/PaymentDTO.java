package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private Integer roomPrice;
    private String roomServiceName;
    private Integer roomServicePrice;
    private Integer memberId;
    private Integer roomId;
    private Integer reservationId; // 예약 ID 추가

    }
