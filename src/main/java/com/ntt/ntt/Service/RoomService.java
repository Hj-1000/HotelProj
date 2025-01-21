package com.ntt.ntt.Service;


import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class RoomService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    //이미지 등록할 RoomImgService 나중에 의존성추가하기



    // 1. 삽입 register
    public Integer registerRoom(RoomDTO roomDTO) {

        // DTO -> Entity 변환
        Room room = modelMapper.map(roomDTO, Room.class);
        // 저장
        Room saveRoom = roomRepository.save(room);

        // 이미지 등록 추가 할 예정


        return saveRoom.getRoomId();
    }
    // 2. 읽기 read
    @Transactional(readOnly = true)
    public RoomDTO readRoom(Integer roomId) {

        // 데이터 조회
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("등록된 아이디에 방을 찾을수 없습니다 : " + roomId));

        return modelMapper.map(room, RoomDTO.class);
    }

    // 3. 읽기 read - 모든방 조회
    public List<RoomDTO> readAllRoom() {
        // 모든 Room 데이터 조회
        List<Room> rooms = roomRepository.findAll();

        // 결과를 담을 RoomDTO 리스트 생성
        List<RoomDTO> roomDTOs = new ArrayList<>();

        // 각 Room 객체를 RoomDTO로 변환하여 리스트에 추가
        for (Room room : rooms) {
            RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);
            roomDTOs.add(roomDTO);
        }

        return roomDTOs;
    }

    // 4. 수정 update
    public void updateRoom(Integer roomId, RoomDTO roomDTO) {
        //Room 존재 확인
        Room existRoom = roomRepository.findById(roomDTO.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("등록된 아이디에 방을 찾을수 없습니다 : " + roomDTO.getRoomId()));

        // 수정한 내용 적용
        existRoom.setRoomName(roomDTO.getRoomName());
        existRoom.setRoomType(roomDTO.getRoomType());
        existRoom.setRoomPrice(roomDTO.getRoomPrice());
        existRoom.setRoomStatus(roomDTO.getRoomStatus());
        existRoom.setRoomInfo(roomDTO.getRoomInfo());

        roomRepository.save(existRoom);
    }

    // 삭제 delete
    public void deleteRoom(Integer roomId) {
        // Room 존재 여부 확인
        if (!roomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Room not found with id: " + roomId);
        }
        // 삭제
        roomRepository.deleteById(roomId);
    }

    // 페이지 네이션

    public Page<RoomDTO> a(Pageable page) {
        int currentPage = page.getPageNumber(); // 현재 페이지 번호
        int pageLimit = page.getPageSize(); // 한 페이지당 데이터 개수

        // 페이지 요청을 생성 (기본 정렬: roomId로 내림차순)
        Pageable pageable = PageRequest.of(currentPage, pageLimit, Sort.by(Sort.Direction.DESC, "roomId"));

        // 페이지 정보에 해당하는 모든 데이터를 읽어옴
        Page<Room> roomEntities = roomRepository.findAll(pageable);

        // Room Entity를 RoomDTO로 변환
        return roomEntities.map(data -> modelMapper.map(data, RoomDTO.class));
    }
}
