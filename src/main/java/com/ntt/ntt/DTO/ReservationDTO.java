package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Reservation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    private String reservationEnd = "";

    private Integer dayCount;

    @Min(1) @Max(6)
    private Integer count;

    // Entity -> DTO 변환 메서드
    public static ReservationDTO fromEntity(Reservation reservation) {
        RoomDTO roomDTO = reservation.getRoom() != null ? RoomDTO.fromEntity(reservation.getRoom()) : null;

        return new ReservationDTO(
                reservation.getReservationId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getTotalPrice(),
                reservation.getReservationStatus(),
                reservation.getMember().getMemberId(),
                reservation.getMember().getMemberName(),
                reservation.getMember().getMemberEmail(),
                reservation.getRoom().getRoomId(),
                roomDTO,
                reservation.getRegDate(),
                reservation.getModDate(),
                roomDTO != null && roomDTO.getReservationEnd() != null && LocalDate.parse(roomDTO.getReservationEnd()).isAfter(LocalDate.now()),
                roomDTO != null ? roomDTO.getReservationEnd() : "",
                reservation.getDayCount(),
                reservation.getCount()
        );
    }
}
