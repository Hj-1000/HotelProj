package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Room;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDTO {

    private Integer roomId;

    private String roomName;

    private String roomType;

    private Integer roomPrice;

    private Boolean roomStatus;

    private String roomInfo;

    private Hotel hotelId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> roomImageDTOList = new ArrayList<>();

    // 추가 필드 포맷된 가격
    private String formattedRoomPrice;

    // 예약 시작일
    private String reservationStart;
    // 예약 종료일
    private String reservationEnd;
    // 숙박 시작일
    private String stayStart;
    // 숙박 종료일
    private String stayEnd;

    private boolean isExpired;

    //Room엔티티 > RoomDTO로 변환 메서드
    public static RoomDTO fromEntity(Room room) {
        return new RoomDTO(
                room.getRoomId(),
                room.getRoomName(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.getRoomStatus(),
                room.getRoomInfo(),
                room.getHotelId(),
                room.getRegDate(),
                room.getModDate(),
                new ArrayList<>(),
                null,
                // 예약 시작일 문자열 변환
                room.getReservationStart() != null ? room.getReservationStart().toString() : null,
                // 예약 종료일 문자열 변환
                room.getReservationEnd() != null ? room.getReservationEnd().toString() : null,
                // 투숙 시작일 문자열 변환
                room.getStayStart() != null ? room.getStayStart().toString() : null,
                // 투숙 종료일 문자열 변환
                room.getStayEnd() != null ? room.getStayEnd().toString() : null,
                // 예약 여부 확인 (예약 시작일이 있으면 true)
                room.getReservationStart() != null
        );
    }

    // reservationEnd 설정 후 checkIfExpired() 호출하여 예약 기간 만료 여부 업데이트
    public void setReservationEnd(String reservationEnd) {
        this.reservationEnd = reservationEnd;
        this.isExpired = checkIfExpired(reservationEnd);
    }
    // 예약 기간 만료 여부 확인
    private boolean checkIfExpired(String reservationEnd) {
        if (reservationEnd == null || reservationEnd.isEmpty()) {
            return false;
        }
        LocalDate endDate = LocalDate.parse(reservationEnd);
        return endDate.isBefore(LocalDate.now());
    }
}
