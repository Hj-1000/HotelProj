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
    // 방 예약 여부
    private Boolean reserved;
    // 예약 기간 만료여부
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
                new ArrayList<>(), // roomImageDTOList 초기화
                null, // formattedRoomPrice 초기화
                room.getReservationStart() != null ? room.getReservationStart().toString() : "",
                room.getReservationEnd() != null ? room.getReservationEnd().toString() : "",
                room.getStayStart() != null ? room.getStayStart().toString() : "",
                room.getStayEnd() != null ? room.getStayEnd().toString() : "",
                room.getReservationStart() != null, // 예약 여부 확인 로직 수정
                checkIfExpired(room.getReservationEnd()) // 예약 기간 만료 여부 체크
        );
    }

    // reservationEnd 설정 후 checkIfExpired() 호출하여 예약 기간 만료 여부 업데이트
    public void setReservationEnd(String reservationEnd) {
        this.reservationEnd = (reservationEnd != null) ? reservationEnd : "";
        this.isExpired = checkIfExpired(this.reservationEnd);
    }

    // 예약 기간 만료 여부 확인
    private static boolean checkIfExpired(String reservationEnd) {
        if (reservationEnd == null || reservationEnd.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate endDate = LocalDate.parse(reservationEnd);
            return endDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}
