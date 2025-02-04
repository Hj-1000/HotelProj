package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Reservation;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {

    private Integer reservationId;

    private String checkInDate;

    private String checkOutDate;

    private Integer totalPrice;

    private String reservationStatus;

    private Integer memberId;

    private String memberName;

    private String memberEmail;

    private Integer roomId;

    private RoomDTO room;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    // 방 예약 여부
    private Boolean reserved;

    // 예약 종료일
    private String reservationEnd;



    // Entity -> DTO 변환 메서드
    public static ReservationDTO fromEntity(Reservation reservation) {
        RoomDTO roomDTO = RoomDTO.fromEntity(reservation.getRoom());
        // 예약자 정보 가져오기
        Member member = reservation.getMember();

        return new ReservationDTO(
                reservation.getReservationId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getTotalPrice(),
                reservation.getReservationStatus(),
                member.getMemberId(),
                member.getMemberName(),
                member.getMemberEmail(),
                reservation.getRoom().getRoomId(),
                roomDTO,
                reservation.getRegDate(),
                reservation.getModDate(),
                roomDTO.getReservationEnd() != null && LocalDate.parse(roomDTO.getReservationEnd()).isAfter(LocalDate.now()),
                roomDTO.getReservationEnd()
        );
    }
}
