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

    private Integer roomPrice; // 객실 가격

    private Integer roomServicePrice; // 룸 서비스 가격

    private Integer totalPrice; // 총 결제금액

    @ManyToOne(fetch = FetchType.LAZY) // Member와 Many-to-One 관계
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) // Member와 Many-to-One 관계
    @JoinColumn(name = "roomId")
    private Room roomId; // 객실 ID

    @ManyToOne(fetch = FetchType.LAZY) // Member와 Many-to-One 관계
    @JoinColumn(name = "reservationId")
    private Reservation reservationId;
}
