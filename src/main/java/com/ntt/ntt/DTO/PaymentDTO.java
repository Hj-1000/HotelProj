package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private Integer paymentId;

    private Integer roomPrice;

    private Integer roomServicePrice;

    private Integer totalPrice;

    private Integer memberId;

    private Integer roomId;

    private Integer reservationId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    }
