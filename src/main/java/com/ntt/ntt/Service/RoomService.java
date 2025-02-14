package com.ntt.ntt.Service;


import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Util.FileUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class RoomService {

    private final ImageRepository imageRepository;
    private final RoomRepository roomRepository;

    private final HotelRepository hotelRepository;

    private final ModelMapper modelMapper;

    private final ReservationRepository reservationRepository;


    // 이미지 등록할 ImageService 의존성 추가
    private final ImageService imageService;
    private final FileUpload fileUpload;

    @Value("${dataUploadPath}")
    private String IMG_LOCATION;


    // 추천 방 목록 가져오기

    public List<RoomDTO> listRecommendedRooms() {
        Pageable pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "roomId"));
        Page<Room> roomsPage = roomRepository.findByRoomStatus(true, pageable);

        log.info("Rooms fetched from repository: {}", roomsPage.getContent());

        List<RoomDTO> roomDTOs = roomsPage.stream()
                .map(room -> {
                    RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);

                    // Room 가격 포맷팅 추가
                    if (room.getRoomPrice() != null) {
                        String formattedPrice = String.format("%,d", room.getRoomPrice());
                        roomDTO.setFormattedRoomPrice(formattedPrice + " KRW");
                    } else {
                        roomDTO.setFormattedRoomPrice("가격 미정");
                    }

                    List<ImageDTO> imagesDTOList = imageRepository.findByRoom_RoomId(room.getRoomId())
                            .stream()
                            .map(image -> {
                                image.setImagePath(image.getImagePath().replace("c:/data/", ""));
                                return modelMapper.map(image, ImageDTO.class);
                            })
                            .collect(Collectors.toList());
                    roomDTO.setRoomImageDTOList(imagesDTOList);
                    return roomDTO;
                })
                .collect(Collectors.toList());

        log.info("Recommended RoomDTOs: {}", roomDTOs);
        return roomDTOs;
    }

    public Page<RoomDTO> getPaginatedRooms(Pageable pageable) {
        // Room 데이터와 이미지를 함께 가져오기 위해 fetch join 사용
        Page<Room> roomEntities = roomRepository.findAllWithImages(pageable);


        // Room Entity를 DTO로 변환
        return roomEntities.map(room -> {
            // 예약 상태 업데이트
            LocalDate today = LocalDate.now(); // 현재 날짜
            if (room.getReservationEnd() != null) {
                LocalDate reservationEndDate = LocalDate.parse(room.getReservationEnd());
                // 오늘 날짜가 reservationEnd 이후라면 예약 불가로 설정
                room.setRoomStatus(!reservationEndDate.isBefore(today));
            }

            RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);

            // Room의 이미지 리스트를 변환
            List<ImageDTO> imageDTOList = room.getRoomImageList().stream()
                    .map(image -> {
                        image.setImagePath(image.getImagePath().replace("c:/data/", "")); // 상대 경로로 변환
                        return modelMapper.map(image, ImageDTO.class);
                    })
                    .collect(Collectors.toList());
            roomDTO.setRoomImageDTOList(imageDTOList);

            // 가격 포맷팅 추가
            if (room.getRoomPrice() != null) {
                roomDTO.setFormattedRoomPrice(String.format("%,d KRW", room.getRoomPrice()));
            } else {
                roomDTO.setFormattedRoomPrice("가격 미정");
            }

            return roomDTO;
        });
    }


    // 모든 방 목록 가져오기 (예약 여부 포함)
    public List<RoomDTO> getRoomListWithReservations() {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream()
                .map(room -> {
                    RoomDTO dto = modelMapper.map(room, RoomDTO.class);
                    dto.setRoomStatus(reservationRepository.existsByRoom_RoomId(room.getRoomId()));
                    dto.setExpired(room.getReservationEnd() != null &&
                            LocalDate.parse(room.getReservationEnd()).isBefore(LocalDate.now()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //호텔 불러오기
    public List<HotelDTO> getAllHotel() {
        List<Hotel> hotels = hotelRepository.findAll();

        List<HotelDTO> hotelDTOS = hotels.stream()
                .map(a -> new HotelDTO(a.getHotelId(), a.getHotelName())).collect(Collectors.toList());

        return hotelDTOS;
    }

    // 1. 등록 register
    public Integer registerRoom(RoomDTO roomDTO, List<MultipartFile> multipartFile) {

        // DTO -> Entity 변환
        Room room = modelMapper.map(roomDTO, Room.class);
        // 저장
        Room saveRoom = roomRepository.save(room);

        //저장된 RoomId를 사용하여 이미지 저장
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageService.registerRoomImage(room.getRoomId(), multipartFile);
        }

        return saveRoom.getRoomId();
    }

    // 2. 조회 read
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

        LocalDate today = LocalDate.now();
        LocalDate newReservationEnd = LocalDate.parse(roomDTO.getReservationEnd());

        // 현재 방에 예약이 있는지 확인
        boolean hasActiveReservation = reservationRepository.existsByRoom_RoomId(roomId);

        // 현재 예약이 있는 경우 , 예약 마감일을 오늘보다 이전으로 수정 할 수 없도록 예외 처리
        if (hasActiveReservation && newReservationEnd.isBefore(today)) {
            throw new IllegalArgumentException("현재 예약이 존재하는 방은 예약 기간을 이전으로 수정할 수 없습니다.");
        }

        // 수정한 내용 적용
        room.setRoomName(roomDTO.getRoomName());
        room.setRoomType(roomDTO.getRoomType());
        room.setRoomPrice(roomDTO.getRoomPrice());
        room.setRoomStatus(roomDTO.getRoomStatus());
        room.setRoomInfo(roomDTO.getRoomInfo());
        room.setReservationStart(roomDTO.getReservationStart());
        room.setReservationEnd(roomDTO.getReservationEnd());
        room.setStayStart(roomDTO.getStayStart());
        room.setStayEnd(roomDTO.getStayEnd());

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

        if (reservationRepository.existsByRoom_RoomId(roomId)) {
            throw new IllegalStateException("이 방은 현재 예약이 존재하므로 삭제할 수 없습니다.");
        }

        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();

            // 룸 연결된 이미지 삭제
            List<Image> imagesToDelete = room.getRoomImageList();
            for (Image image : imagesToDelete) {
                // 이미지 서비스에서 물리적 파일 삭제 + DB에서 삭제
                imageService.deleteImage(image.getImageId());
            }

            roomRepository.deleteById(roomId);
        } else {
            throw new RuntimeException("회사를 찾을 수 없습니다.");
        }
    }

    /* 룸 페이지 검색 메서드*/

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

    /* 예약 페이지 검색 메서드*/
    @Transactional(readOnly = true)
    public Page<RoomDTO> searchAllRooms(String keyword, String category, Pageable pageable) {
        Page<Room> rooms;

        if (keyword == null || keyword.isEmpty()) {
            rooms = roomRepository.findAll(pageable);
        } else {
            switch (category) {
                case "roomName":
                    rooms = roomRepository.findByRoomNameContaining(keyword, pageable);
                    break;
                case "status":
                    if ("빈방".equalsIgnoreCase(keyword)) {
                        rooms = roomRepository.findAvailableRooms(pageable); // 빈방만 검색
                    } else if ("예약".equalsIgnoreCase(keyword)) {
                        rooms = roomRepository.findReservedRooms(pageable); // 모든 예약된 방 검색
                    } else if ("기간 만료".equalsIgnoreCase(keyword)) {
                        rooms = roomRepository.findExpiredRooms(pageable); // 기간 만료 검색
                    } else {
                        throw new IllegalArgumentException("유효하지 않은 상태 키워드: 빈방, 예약, 기간 만료 중 선택하세요.");
                    }
                    break;
                default:
                    rooms = roomRepository.findAll(pageable);
            }
        }

        return rooms.map(room -> {
            RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);

            // 예약 기간 만료 여부 계산
            if (room.getReservationEnd() != null) {
                LocalDate reservationEndDate = LocalDate.parse(room.getReservationEnd());
                roomDTO.setExpired(reservationEndDate.isBefore(LocalDate.now()));
            }

            // 이미지 리스트 매핑
            List<ImageDTO> imageDTOList = imageRepository.findByRoom_RoomId(room.getRoomId())
                    .stream()
                    .map(image -> {
                        image.setImagePath(image.getImagePath().replace("c:/data/", ""));
                        return modelMapper.map(image, ImageDTO.class);
                    })
                    .collect(Collectors.toList());

            roomDTO.setRoomImageDTOList(imageDTOList);
            return roomDTO;
        });
    }

    // 방 강제 사용 중지 (관리자 기능)
    public void disableRoom(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));
        room.setRoomStatus(false);
        roomRepository.save(room);
    }

    // hotelId에 맞는 방들을 가져오는 메서드
    public Page<RoomDTO> getRoomsByHotelId(Integer hotelId, Pageable pageable) {
        // 호텔 ID에 맞는 Room 데이터 조회 (페이징 처리)
        Page<Room> roomsPage = roomRepository.findByHotelId_HotelId(hotelId, pageable);

        // 각 Room 객체를 RoomDTO로 변환하여 리스트에 추가
        List<RoomDTO> roomDTOs = roomsPage.getContent().stream()
                .map(room -> modelMapper.map(room, RoomDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<>(roomDTOs, pageable, roomsPage.getTotalElements());
    }


    public void updateReservationEnd(Integer roomId, String reservationEnd) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        // 예약 마감 날짜가 비어있으면 기본값 설정
        if (reservationEnd == null || reservationEnd.trim().isEmpty()) {
            throw new IllegalArgumentException("예약 마감 날짜가 필요합니다.");
        }

        room.setReservationEnd(reservationEnd);
        roomRepository.save(room);
    }


    public void updateRoomStatusBasedOnReservationEnd(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        // 오늘 날짜와 비교하여 객실 상태 업데이트
        LocalDate today = LocalDate.now();
        LocalDate reservationEndDate = LocalDate.parse(room.getReservationEnd());

        if (reservationEndDate.isBefore(today)) {
            room.setRoomStatus(false); // 예약 불가
        } else {
            room.setRoomStatus(true); // 예약 가능
        }

        roomRepository.save(room);
    }
}
