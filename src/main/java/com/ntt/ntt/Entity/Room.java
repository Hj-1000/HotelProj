package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name="room")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;
    // 객실 이름
    @Column(length = 50, nullable = false)
    private String roomName;
    // 객실 타입(싱글, 더블, 스위트룸, ...)
    @Column(length = 50, nullable = false)
    private String roomType;
    // 객실 가격
    @Column(nullable = false)
    private Integer roomPrice;
    // 객실 상태(예약 가능 여부, true/false)
    @Column(nullable = false)
    private Boolean roomStatus;
    // 객실 설명
    @Column(length = 255)
    private String roomInfo;

    // 예약 시작일
    @Column(nullable = true)
    private String reservationStart;

    // 예약 종료일
    @Column(nullable = true)
    private String reservationEnd;

    // 숙박 시작일
    @Column(nullable = true)
    private String stayStart;

    // 숙박 종료일
    @Column(nullable = true)
    private String stayEnd;

    @ManyToOne
    @JoinColumn(name = "hotelId")
    private Hotel hotelId;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Image> roomImageList;

    public Room(Integer roomId) {
        this.roomId = roomId;
    }

    public void updateRoomStatus() {
        LocalDate today = LocalDate.now();
        if (this.reservationEnd != null) {
            LocalDate reservationEndDate = LocalDate.parse(this.reservationEnd);
            this.roomStatus = reservationEndDate.isAfter(today) || reservationEndDate.isEqual(today);
        }
    }
}
