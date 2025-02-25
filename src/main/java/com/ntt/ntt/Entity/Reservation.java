package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    @Column(nullable = false)
    private String checkInDate;
    // 체크아웃 날짜
    @Column(nullable = false)
    private String checkOutDate;
    // 총 예약 금액
    private Integer totalPrice;
    // 예약 상태
    @Column(length = 50, nullable = false)
    private String reservationStatus;
    // 관리자가 취소 완료를 승인한 시간 (자동 삭제 타이머)
    @Column(name = "cancel_confirmed_at", nullable = true)
    private LocalDateTime cancelConfirmedAt;
    // 숙박일 수
    @Column(nullable = false)
    private Integer dayCount = 1;
    //예약 인원수
    @Column(nullable = false)
    private Integer count;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room room;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Payment> payments;

}
