package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Reservation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
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
    // 예약마감기간
    private String reservationEnd = "";
    // 숙박일
    private Integer dayCount;
    // 인원
    @Min(1) @Max(6)
    private Integer count;
    // 관리자가 취소 승인한 시간
    private LocalDateTime cancelConfirmedAt;
    // 남은 시간(초 단위)
    private Long timeRemaining;

    // Entity -> DTO 변환 메서드
    public static ReservationDTO fromEntity(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation entity가 null입니다.");
        }

        RoomDTO roomDTO = reservation.getRoom() != null ? RoomDTO.fromEntity(reservation.getRoom()) : null;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancelConfirmedTime = reservation.getCancelConfirmedAt();
        Long remainingTime = 0L; // 기본값을 0으로 설정

        if ("취소 완료".equals(reservation.getReservationStatus()) && cancelConfirmedTime != null) {
            remainingTime = Duration.between(now, cancelConfirmedTime.plusMinutes(1)).toSeconds(); // 1분 후 삭제

            if (remainingTime < 0) {
                remainingTime = 0L; // 자동 삭제된 경우 0으로 설정
            }
        }

        log.info("[fromEntity] 변환 - reservationId={}, cancelConfirmedAt={}, remainingTime={}",
                reservation.getReservationId(), cancelConfirmedTime, remainingTime);

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
                reservation.getCount(),
                cancelConfirmedTime,
                remainingTime
        );
    }
}
