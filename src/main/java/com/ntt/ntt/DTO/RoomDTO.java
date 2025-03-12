package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Entity.Room;
import jakarta.validation.constraints.NotNull;
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

    @Builder.Default
    private Boolean roomStatus = true;

    private String roomInfo;

    @NotNull(message = "지사를 선택해야 합니다.")
    private Hotel hotelId;

    private String hotelName;

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

    // 배너(대표) 이미지 경로
    private String bannerImage;


    //Room엔티티 > RoomDTO로 변환 메서드
    public static RoomDTO fromEntity(Room room) {
        List<ImageDTO> imageDTOList = new ArrayList<>();
        String bannerImagePath = "";  // 배너 이미지 기본값

        if (room.getRoomImageList() != null) {
            for (Image image : room.getRoomImageList()) {
                if ("Y".equals(image.getImageMain())) {
                    bannerImagePath = image.getImagePath(); // 배너 이미지 저장
                } else {
                    imageDTOList.add(ImageDTO.fromEntity(image)); //  상세 이미지 리스트에 추가
                }
            }
        }

        //  상세 이미지 리스트 로그 출력 (디버깅용)
        System.out.println(" 상세 이미지 개수: " + imageDTOList.size());

        return new RoomDTO(
                room.getRoomId(),
                room.getRoomName(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.getRoomStatus(),
                room.getRoomInfo(),
                room.getHotelId(),
                room.getHotelId() != null ? room.getHotelId().getHotelName() : "없음",
                room.getRegDate(),
                room.getModDate(),
                imageDTOList, // 상세 이미지 리스트 포함
                null,
                room.getReservationStart() != null ? room.getReservationStart().toString() : "",
                room.getReservationEnd() != null ? room.getReservationEnd().toString() : "",
                room.getStayStart() != null ? room.getStayStart().toString() : "",
                room.getStayEnd() != null ? room.getStayEnd().toString() : "",
                room.getReservationStart() != null,
                checkIfExpired(room.getReservationEnd()),
                bannerImagePath // 배너 이미지 저장
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
