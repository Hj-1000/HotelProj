package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name="payment")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId; // 결제 ID

    @Column(nullable = true)
    private Integer roomPrice; // 객실 가격

    private String roomServiceName; // 룸 서비스 이름

    @Column(nullable = true)
    private Integer roomServicePrice; // 룸 서비스 가격

    @Column(nullable = true)
    private Integer memberId; // 회원 ID

    @Column(nullable = true)
    private Integer roomId; // 객실 ID

    private Integer reservationId;
}
