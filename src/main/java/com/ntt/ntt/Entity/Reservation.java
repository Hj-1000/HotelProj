package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="reservation")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reservationId;
    // 체크인 날짜
    private String checkInDate;
    // 체크아웃 날짜
    private String checkOutDate;
    // 총 예약 금액
    private Integer totalPrice;
    // 예약 상태
    @Column(length = 50)
    private String reservationStatus;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User userId;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room roomId;

}
