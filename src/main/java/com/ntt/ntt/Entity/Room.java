package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

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
    // 객실 사진
    private String roomImg;

    @ManyToOne
    @JoinColumn(name = "hotelId")
    private Hotel hotelId;

}
