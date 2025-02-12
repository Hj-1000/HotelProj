package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    //기본 삽입,수정,삭제는 추가로 작업할 내용이 없다.
    //조회만 사용 목적에 따라서 작성
    //Query를 이용해서 작성 방법 : Entity와 테이블을 직접 접근
    //JPA를 이용해서 작성 방법 : findBy필드[조건][정렬][논리연산][관계연산]
    //조회처리

    /* 방 이름으로 검색하여 페이징된 결과 반환 , 특정 문자열이 포함된 방을 찾음 */
    Page<Room> findByRoomNameContaining(String roomName, Pageable pageable);

    /* 방 타입으로 검색하여 페이징된 결과 반환 , 특정 문자열이 포함된 방을 찾음*/
    Page<Room> findByRoomTypeContaining(String roomType, Pageable pageable);

    /* 특정 상태(roomStatus)의 방을 페이징하여 조회 , true : 예약 , false : 예약 불가 */
    Page<Room> findByRoomStatus(boolean roomStatus, Pageable pageable);

    /* 모든 방 목록을 페이징 처리하여 조회*/
    Page<Room> findAll(Pageable pageable);

    /* 방 목록을 조회하면서 각 방과 연결된 이미지 리스트를 함께 가져오기 */
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.roomImageList")
    Page<Room> findAllWithImages(Pageable pageable);

    /* 빈방 조회 */
    @Query("SELECT r FROM Room r WHERE r.roomStatus = true AND NOT EXISTS (SELECT 1 FROM Reservation res WHERE res.room.roomId = r.roomId)")
    Page<Room> findAvailableRooms(Pageable pageable);

    /* 예약된 방 조회 */
    @Query("SELECT r FROM Room r WHERE EXISTS (SELECT 1 FROM Reservation res WHERE res.room.roomId = r.roomId)")
    Page<Room> findReservedRooms(Pageable pageable);

    /* 기간 만료된 방 조회 */
    @Query("SELECT r FROM Room r WHERE r.reservationEnd < FUNCTION('FORMATDATETIME', CURRENT_DATE, 'yyyy-MM-dd')")
    Page<Room> findExpiredRooms(Pageable pageable);

    /* 특정 호텔 ID에 속한 방들을 페이징 처리후 조회*/
    Page<Room> findByHotelId_HotelId (Integer hotelId, Pageable pageable);

}
