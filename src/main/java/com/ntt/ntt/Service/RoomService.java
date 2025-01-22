package com.ntt.ntt.Service;


import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Util.FileUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class RoomService {

    private final ImageRepository imageRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    //이미지 등록할 RoomImgService 나중에 의존성추가하기

    // 이미지 등록할 ImageService 의존성 추가
    private final ImageService imageService;
    private final FileUpload fileUpload;

    @Value("${dataUploadPath}")
    private String IMG_LOCATION;

    // 1. 삽입 register
    public Integer registerRoom(RoomDTO roomDTO, List<MultipartFile> multipartFile) {

        // DTO -> Entity 변환
        Room room = modelMapper.map(roomDTO, Room.class);
        // 저장
        Room saveRoom = roomRepository.save(room);

        //저장된 RoomId를 사용하여 이미지 저장
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageService.registerRoomImage(room.getRoomId(), multipartFile);
        }
        log.info("잘 들어 왔나요?" + room);
        log.info("컨트롤러에서 들어온 이미지 정보" + multipartFile);
        log.info("RoomId object after mapping: {}", room);

        return saveRoom.getRoomId();
    }
    // 2. 읽기 read
    @Transactional(readOnly = true)
    public RoomDTO readRoom(Integer roomId) {
        // 데이터 조회
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 아이디에 방을 찾을 수 없습니다: " + roomId));

        // Room -> RoomDTO 변환
        RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);

        // 이미지 조회 및 설정
        List<ImageDTO> imageDTOList = imageRepository.findByRoom_RoomId(roomId)
                .stream()
                .map(image -> {
                    // 이미지 경로 상대 경로 변환
                    image.setImagePath(image.getImagePath().replace("c:/data/", ""));
                    return modelMapper.map(image, ImageDTO.class);
                })
                .collect(Collectors.toList());

        roomDTO.setRoomImageDTOList(imageDTOList);

        return roomDTO;
    }

    // 3. 목록 list
    public List<RoomDTO> listRoom() {
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
    public void updateRoom(Integer roomId, RoomDTO roomDTO, List<MultipartFile> imageFile) {
        //Room 존재 확인
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 아이디에 방을 찾을수 없습니다 : " + roomId));

        // 수정한 내용 적용
        room.setRoomName(roomDTO.getRoomName());
        room.setRoomType(roomDTO.getRoomType());
        room.setRoomPrice(roomDTO.getRoomPrice());
        room.setRoomStatus(roomDTO.getRoomStatus());
        room.setRoomInfo(roomDTO.getRoomInfo());

        // 기존 이미지를 삭제
        if (imageFile != null && !imageFile.isEmpty()) {
            // Room에 연결된 모든 이미지를 가져온다
            List<ImageDTO> existingImages = imageRepository.findByRoom_RoomId(roomId)
                    .stream()
                    .map(image -> {
                        // 이미지 경로를 삭제 전에 로그로 확인
                        log.info("Deleting image with path: {}", image.getImagePath());
                        return modelMapper.map(image, ImageDTO.class);
                    })
                    .collect(Collectors.toList());

            // 각 이미지 엔티티를 삭제
            existingImages.forEach(imageDTO -> {
                imageRepository.deleteById(imageDTO.getImageId()); // 이미지 ID를 기반으로 삭제
                log.info("Image deleted successfully with ID: {}", imageDTO.getImageId());
            });

            // 4. 새로운 이미지 저장
            imageService.registerRoomImage(roomId, imageFile); // ImageService 활용
        }

        // Room 저장
        roomRepository.save(room);
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

    @Transactional(readOnly = true)
    public Page<RoomDTO> searchRooms(String keyword, String category, Pageable pageable) {
        Page<Room> rooms;

        if (keyword == null || keyword.isEmpty()) {
            rooms = roomRepository.findAll(pageable);
        } else {
            switch (category) {
                case "roomName":
                    rooms = roomRepository.findByRoomNameContaining(keyword, pageable);
                    break;
                case "roomType":
                    rooms = roomRepository.findByRoomTypeContaining(keyword, pageable);
                    break;
                case "roomStatus":
                    Boolean status = "Available".equalsIgnoreCase(keyword) ? true :
                            "Unavailable".equalsIgnoreCase(keyword) ? false : null;
                    if (status != null) {
                        rooms = roomRepository.findByRoomStatus(status, pageable);
                    } else {
                        throw new IllegalArgumentException("Invalid room status keyword: " + keyword);
                    }
                    break;
                default:
                    rooms = roomRepository.findAll(pageable);
            }
        }

        // Room Entity를 RoomDTO로 변환하면서 이미지 리스트 추가
        return rooms.map(room -> {
            RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);

            // 이미지 리스트 매핑
            List<ImageDTO> imageDTOList = imageRepository.findByRoom_RoomId(room.getRoomId())
                    .stream()
                    .map(image -> {
                        // 이미지 경로 수정 (예: 절대 경로 -> 상대 경로)
                        image.setImagePath(image.getImagePath().replace("c:/data/", ""));
                        return modelMapper.map(image, ImageDTO.class);
                    })
                    .collect(Collectors.toList());

            roomDTO.setRoomImageDTOList(imageDTOList);
            return roomDTO;
        });
    }
}
