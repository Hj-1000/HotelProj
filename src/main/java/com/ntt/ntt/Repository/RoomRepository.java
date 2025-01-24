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

    Page<Room> findByRoomNameContaining(String roomName, Pageable pageable);

    Page<Room> findByRoomTypeContaining(String roomType, Pageable pageable);

    Page<Room> findByRoomStatus(boolean roomStatus, Pageable pageable);

    Page<Room> findAll(Pageable pageable);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.roomImageList")
    Page<Room> findAllWithImages(Pageable pageable);
}
