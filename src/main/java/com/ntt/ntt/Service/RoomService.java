package com.ntt.ntt.Service;


import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Repository.RoomReviewRepository;
import com.ntt.ntt.Repository.company.CompanyRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Util.FileUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
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
    private final RoomReviewRepository roomReviewRepository;
    private final HotelRepository hotelRepository;
    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    private final ReservationRepository reservationRepository;
    private final MemberService memberService;


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
    public List<HotelDTO> getAllHotel(Authentication authentication) {
        // 로그인한 사용자 ID 가져오기
        Integer memberId = getLoggedInMemberId(authentication);
        Member member = memberService.findById(memberId);

        if (member == null) {
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
        }

        List<Hotel> hotels;

        // 권한별 호텔 목록 필터링
        if (member.getRole() == Role.ADMIN) {
            // ADMIN → 모든 호텔(지점) 조회
            hotels = hotelRepository.findAll();
        } else if (member.getRole() == Role.CHIEF) {
            // CHIEF → 본인의 본사(Company)에 속한 호텔(지점)들만 조회
            hotels = hotelRepository.findByCompanyByMemberByMemberId(memberId);
        } else if (member.getRole() == Role.MANAGER) {
            // MANAGER → 본인이 속한 지점 1개만 조회
            hotels = hotelRepository.findHotelsByManagerId(memberId);
        } else {
            throw new RuntimeException("올바르지 않은 권한입니다.");
        }

        // Entity → DTO 변환
        return hotels.stream()
                .map(hotel -> new HotelDTO(hotel.getHotelId(), hotel.getHotelName()))
                .collect(Collectors.toList());
    }

    /* 배너 이미지 수정 메서드 */
    public void updateRoomBannerImage(Integer roomId, MultipartFile bannerImageFile) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다: " + roomId));

        // 기존 배너 이미지 삭제 (새로운 배너가 존재할 때만 삭제)
        if (bannerImageFile != null && !bannerImageFile.isEmpty()) {
            imageRepository.findByRoom_RoomId(roomId).stream()
                    .filter(image -> "Y".equals(image.getImageMain()))
                    .forEach(image -> {
                        fileUpload.FileDelete(IMG_LOCATION, image.getImagePath());
                        imageRepository.delete(image);
                    });

            // 새로운 배너 이미지 저장
            String savedPath = imageService.registerRoombannerImage(bannerImageFile, roomId);
            Image bannerImage = Image.builder()
                    .imagePath(savedPath)
                    .imageMain("Y") // 대표 이미지 설정
                    .room(room)
                    .build();
            imageRepository.save(bannerImage);
        }
    }

    /* 이미지 타이틀과 설명 수정 메서드 */
    @Transactional
    public void updateRoomImageDetails(List<Integer> existingImageIds, List<String> existingImageTitles, List<String> existingImageDescriptions) {
        for (int i = 0; i < existingImageIds.size(); i++) {
            Integer imageId = existingImageIds.get(i);
            Optional<Image> optionalImage = imageRepository.findById(imageId);

            if (optionalImage.isPresent()) {
                Image image = optionalImage.get();

                // 비어 있을경우
                String title = (existingImageTitles.size() > i && existingImageTitles.get(i) != null && !existingImageTitles.get(i).trim().isEmpty())
                        ? existingImageTitles.get(i) : "타이틀을 입력해주세요";

                String description = (existingImageDescriptions.size() > i && existingImageDescriptions.get(i) != null && !existingImageDescriptions.get(i).trim().isEmpty())
                        ? existingImageDescriptions.get(i) : "설명을 입력해주세요";

                log.info("기존 이미지 수정 - ID: {}, 기존 제목: {}, 새로운 제목: {}, 기존 설명: {}, 새로운 설명: {}",
                        imageId, image.getImageTitle(), title, image.getImageDescription(), description);

                image.setImageTitle(title);
                image.setImageDescription(description);
                imageRepository.save(image);
                log.info("이미지 정보 업데이트됨 - ID: {}, 제목: {}, 설명: {}", imageId, title, description);
            } else {
                log.warn("이미지를 찾을 수 없습니다. ID: {}", imageId);
            }
        }
    }

    // 1. 등록 register
    public Integer registerRoom(RoomDTO roomDTO,
                                List<MultipartFile> multipartFile,
                                MultipartFile bannerImageFile,
                                List<String> imageTitles,
                                List<String> imageDescriptions) {

        // 이미지 제목과 설명이 null일 경우 빈 리스트로 초기화하여 데이터 저장 시 예외 방지
        if (imageTitles == null || imageTitles.isEmpty()) {
            imageTitles = new ArrayList<>();
        }

        if (imageDescriptions == null || imageDescriptions.isEmpty()) {
            imageDescriptions = new ArrayList<>();
        }

        // DTO -> Entity 변환
        Room room = modelMapper.map(roomDTO, Room.class);
        // 저장
        Room saveRoom = roomRepository.save(room);

        // 객실 이미지 저장
        if (multipartFile != null && !multipartFile.isEmpty()) {
            List<String> filteredTitles = new ArrayList<>();
            List<String> filteredDescriptions = new ArrayList<>();
            List<MultipartFile> filteredFiles = new ArrayList<>();

            for (int i = 0; i < multipartFile.size(); i++) {
                MultipartFile file = multipartFile.get(i);
                String title = (imageTitles != null && i < imageTitles.size()) ? imageTitles.get(i) : "";
                String description = (imageDescriptions != null && i < imageDescriptions.size()) ? imageDescriptions.get(i) : "";

                // 이미지 파일이 없고, 제목/설명만 있으면 제외
                if (file == null || file.isEmpty()) {
                    log.warn("이미지 파일 없이 제목({}) 또는 설명({})만 존재하여 제외됨", title, description);
                    continue;
                }

                filteredFiles.add(file);
                filteredTitles.add(title);
                filteredDescriptions.add(description);
            }

            // 필터링된 이미지만 저장
            if (!filteredFiles.isEmpty()) {
                imageService.registerRoomImage(saveRoom.getRoomId(), filteredFiles, filteredTitles, filteredDescriptions);
            }
        }

        // 배너 이미지 저장
        if (bannerImageFile != null && !bannerImageFile.isEmpty()) {
            String savedPath = imageService.registerRoombannerImage(bannerImageFile, saveRoom.getRoomId());

            // ** 기존 배너 이미지가 없을 때만 저장 **
            boolean isBannerExists = imageRepository.findByRoom_RoomIdAndImageMain(saveRoom.getRoomId(), "Y") != null;
            if (!isBannerExists) {
                Image bannerImage = Image.builder()
                        .imagePath(savedPath)
                        .imageMain("Y") // 대표 이미지 설정
                        .room(saveRoom)
                        .build();
                imageRepository.save(bannerImage);
            }
        }

        log.info("[registerRoom] 실행됨 - Room ID: {}", saveRoom.getRoomId());

        return saveRoom.getRoomId();
    }

    // 2. 조회 read
    @Transactional(readOnly = true)
    public RoomDTO readRoom(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 아이디에 방을 찾을 수 없습니다: " + roomId));

        RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);

        // 가격 포맷팅
        if (room.getRoomPrice() != null) {
            roomDTO.setFormattedRoomPrice(String.format("%,d KRW", room.getRoomPrice()));
        } else {
            roomDTO.setFormattedRoomPrice("가격 미정");
        }

        List<ImageDTO> imageDTOList = new ArrayList<>();
        String bannerImagePath = "";  // 배너 이미지 기본값

        // 이미지 조회 및 배너 이미지와 상세 이미지를 분리 저장
        for (Image image : imageRepository.findByRoom_RoomId(roomId)) {
            if ("Y".equals(image.getImageMain())) {
                bannerImagePath = image.getImagePath(); // 배너 이미지 저장
            } else {
                ImageDTO imageDTO = modelMapper.map(image, ImageDTO.class);

                // 기존 이미지 제목과 설명을 DTO에 반영하도록 추가
                imageDTO.setImageTitle(image.getImageTitle());
                imageDTO.setImageDescription(image.getImageDescription());

                log.info("기존 이미지 정보 - ID: {}, 제목: {}, 설명: {}",
                        imageDTO.getImageId(), imageDTO.getImageTitle(), imageDTO.getImageDescription());

                imageDTOList.add(imageDTO); // 상세 이미지 리스트에 추가
            }
        }

        //  상세 이미지 리스트가 비어있는지 확인
        log.warn(" 상세 이미지 개수 (DB 조회 결과): {}", imageDTOList.size());

        roomDTO.setRoomImageDTOList(imageDTOList); //  상세 이미지 설정
        roomDTO.setBannerImage(bannerImagePath); // 배너 이미지 설정

        return roomDTO;
    }


    // 3. 목록 list
    @Transactional(readOnly = true)
    public Page<RoomDTO> listRooms(String keyword, String category, Pageable pageable,Authentication authentication) {
        log.info(" 검색 요청 - 카테고리: {}, 키워드: {}", category, keyword);

        // 로그인한 사용자 정보 가져오기
        Integer memberId = getLoggedInMemberId(authentication);
        Member member = memberService.findById(memberId);

        if (member == null) {
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
        }

        Page<Room> rooms;

        //  ADMIN(최고 관리자) → 모든 객실 조회
        if (member.getRole() == Role.ADMIN) {
            rooms = roomRepository.findAll(pageable);
        }
        //  CHIEF(본점 관리자) → 본점의 모든 지점 객실 조회
        else if (member.getRole() == Role.CHIEF) {
            rooms = roomRepository.findByHotel_Company_Member_MemberId(memberId, pageable);
        }
        //  MANAGER(지점 관리자) → 본인이 속한 지점 객실만 조회
        else if (member.getRole() == Role.MANAGER) {
            rooms = roomRepository.findByHotel_Member_MemberId(memberId, pageable);
        }
        // 예외 처리
        else {
            throw new RuntimeException("올바르지 않은 권한입니다.");
        }

        //  2. 검색어(keyword)가 존재하는 경우 필터링
        if (keyword != null && !keyword.isEmpty()) {
            switch (category) {
                case "roomName":
                    rooms = roomRepository.findByRoomNameContaining(keyword, pageable);
                    break;
                case "roomType":
                    rooms = roomRepository.findByRoomTypeContaining(keyword.toLowerCase(), pageable);
                    break;
                case "roomStatus":
                    Boolean status = null;
                    if ("av".equalsIgnoreCase(keyword) || "available".equalsIgnoreCase(keyword)) {
                        status = true;
                    } else if ("un".equalsIgnoreCase(keyword) || "unavailable".equalsIgnoreCase(keyword)) {
                        status = false;
                    }
                    log.info("🔍 변환된 상태 값: {}", status);
                    if (status != null) {
                        rooms = roomRepository.findByRoomStatus(status, pageable);
                    } else {
                        throw new IllegalArgumentException("유효하지 않은 상태 값입니다. 'av', 'un', 'available', 'unavailable'만 입력 가능합니다.");
                    }
                    break;
                default:
                    log.warn(" 잘못된 검색 카테고리: {}", category);
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

    // 4. 수정 update
    public void updateRoom(Integer roomId, RoomDTO roomDTO, List<MultipartFile> imageFile,
                           List<String> imageTitles, List<String> imageDescriptions,
                           List<Integer> deleteImages,
                           List<Integer> existingImageIds, List<String> existingImageTitles, List<String> existingImageDescriptions) {

        log.info("받은 이미지 제목 리스트: {}", existingImageTitles);
        log.info("받은 이미지 설명 리스트: {}", existingImageDescriptions);

        // 기존 이미지 제목 및 설명이 null이면 빈 리스트로 초기화
        if (existingImageTitles == null || existingImageTitles.isEmpty()) {
            existingImageTitles = new ArrayList<>();
        }

        if (existingImageDescriptions == null || existingImageDescriptions.isEmpty()) {
            existingImageDescriptions = new ArrayList<>();
        }

        //Room 존재 확인
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 아이디에 방을 찾을수 없습니다 : " + roomId));

        LocalDate today = LocalDate.now();
        LocalDate newReservationEnd = roomDTO.getReservationEnd() != null ? LocalDate.parse(roomDTO.getReservationEnd()) : null;

        // 현재 방에 예약이 있는지 확인
        boolean reserved = reservationRepository.existsByRoom_RoomId(roomId);

        // 현재 예약이 있는 경우 , 예약 마감일을 오늘보다 이전으로 수정 할 수 없도록 예외 처리
        if (reserved && newReservationEnd != null && newReservationEnd.isBefore(today)) {
            throw new IllegalArgumentException("현재 예약이 존재하는 방은 예약 기간을 오늘 이전으로 수정할 수 없습니다.");
        }

        // roomStatus가 null이면 기본값 설정
        if (roomDTO.getRoomStatus() == null) {
            room.setRoomStatus(true);
        } else {
            room.setRoomStatus(roomDTO.getRoomStatus());
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

        log.info("Room ID: {} 수정됨. 새로운 예약 종료일: {}", roomId, room.getReservationEnd());

        // 기존 이미지 제목 및 설명 업데이트 추가
        if (existingImageIds != null && !existingImageIds.isEmpty()) {
            log.info("이미지 업데이트 요청 - ID 목록: {}", existingImageIds);
            log.info("이미지 제목 목록: {}", existingImageTitles);
            log.info("이미지 설명 목록: {}", existingImageDescriptions);

            for (int i = 0; i < existingImageIds.size(); i++) {
                Integer imageId = existingImageIds.get(i);
                Optional<Image> optionalImage = imageRepository.findById(imageId);

                if (optionalImage.isPresent()) {
                    Image image = optionalImage.get();

                    // 제목과 설명이 비어있다면 기본값으로 설정
                    String title = (existingImageTitles.size() > i && existingImageTitles.get(i) != null && !existingImageTitles.get(i).trim().isEmpty())
                            ? existingImageTitles.get(i)
                            : "제목 없음";

                    String description = (existingImageDescriptions.size() > i && existingImageDescriptions.get(i) != null && !existingImageDescriptions.get(i).trim().isEmpty())
                            ? existingImageDescriptions.get(i)
                            : "설명 없음";

                    // 기존 이미지의 제목과 설명 업데이트
                    image.setImageTitle(title);
                    image.setImageDescription(description);

                    log.info("이미지 정보 업데이트됨 - ID: {}, 제목: {}, 설명: {}", imageId, image.getImageTitle(), image.getImageDescription());

                    // DB 저장
                    imageRepository.save(image);
                } else {
                    log.warn("이미지를 찾을 수 없습니다. ID: {}", imageId);
                }
            }
        }

        // 기존 이미지를 삭제
        if (deleteImages != null && !deleteImages.isEmpty()) {
            for (Integer imageId : deleteImages) {
                Optional<Image> optionalImage = imageRepository.findById(imageId);

                if (optionalImage.isPresent()) {
                    Image image = optionalImage.get();
                    String imagePath = image.getImagePath();
                    String imageFileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);

                    log.info("Deleting selected image: {}", imagePath);

                    // 물리적 파일 삭제
                    fileUpload.FileDelete(IMG_LOCATION, imageFileName);
                    log.info("Image file deletion attempted: {}", imageFileName);

                    // DB에서 이미지 삭제
                    imageRepository.deleteById(imageId);
                    log.info("Image record deleted from DB: Image ID {}", imageId);
                } else {
                    log.warn("이미지를 찾을 수 없습니다. ID: {}", imageId);
                }
            }
        }

        // 새로운 이미지 저장 (제목 & 설명 포함)
        if (imageFile != null && !imageFile.isEmpty()) {
            log.info("Room ID: {} 새로운 이미지 저장 시작", roomId);

            List<String> filteredTitles = new ArrayList<>();
            List<String> filteredDescriptions = new ArrayList<>();
            List<MultipartFile> filteredFiles = new ArrayList<>();

            for (int i = 0; i < imageFile.size(); i++) {
                MultipartFile file = imageFile.get(i);

                // 새로운 이미지의 제목 및 설명 가져오기
                String title = (imageTitles != null && imageTitles.size() > i) ? imageTitles.get(i) : "제목 없음";
                String description = (imageDescriptions != null && imageDescriptions.size() > i) ? imageDescriptions.get(i) : "설명 없음";

                // 이미지 파일이 없고, 제목/설명만 존재하면 제외
                if (file == null || file.isEmpty()) {
                    log.warn("이미지 파일 없이 제목({}) 또는 설명({})만 존재하여 제외됨", title, description);
                    continue;
                }

                filteredFiles.add(file);
                filteredTitles.add(title);
                filteredDescriptions.add(description);
            }

            // 필터링된 이미지만 저장
            if (!filteredFiles.isEmpty()) {
                imageService.registerRoomImage(roomId, filteredFiles, filteredTitles, filteredDescriptions);
            } else {
                log.warn("새로운 이미지가 없으므로 추가 저장하지 않음.");
            }
        }


        // Room 저장
        roomRepository.save(room);
        log.info("Room ID: {} 업데이트 완료", roomId);
    }

    // 삭제 delete
    public void deleteRoom(Integer roomId) {

        if (reservationRepository.existsByRoom_RoomId(roomId)) {
            throw new IllegalStateException("이 방은 현재 예약이 존재하므로 삭제할 수 없습니다.");
        }

        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();

            // 룸 연결된 리뷰 삭제
            try {
                roomReviewRepository.deleteByRoom_RoomId(roomId);
            } catch (Exception e) {
                throw new RuntimeException("방에 연결된 리뷰 삭제 중 오류 발생: " + e.getMessage());
            }

            // 룸 연결된 이미지 삭제 (예외 발생 방지)
            List<Image> imagesToDelete = room.getRoomImageList();
            for (Image image : imagesToDelete) {
                try {
                    // 이미지 존재 여부 확인 후 삭제
                    if (imageRepository.existsById(image.getImageId())) {
                        imageService.delImage(image.getImageId());
                    } else {
                        log.warn("이미지 삭제 건너뜀 (이미지 ID: {}): 해당 이미지가 존재하지 않음", image.getImageId());
                    }
                } catch (Exception e) {
                    log.warn("이미지 삭제 중 오류 발생 (이미지 ID: {}): {}", image.getImageId(), e.getMessage());
                }
            }

            // 룸 삭제
            try {
                roomRepository.deleteById(roomId);
            } catch (Exception e) {
                throw new RuntimeException("방 삭제 중 오류 발생: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("해당 방을 찾을 수 없습니다.");
        }
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

    // 객실 ID에 해당하는 객실을 조회하고, 해당 객실의 호텔 ID를 가져오는 메서드 2025-03-06
    public Integer getHotelIdByRoomId(Integer roomId) {
        // 객실 ID로 객실을 찾고, 해당 객실의 호텔 ID를 리턴
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("객실을 찾을 수 없습니다."));
        return room.getHotelId().getHotelId();  // 호텔 ID 반환
    }


    public void updateReservationEnd(Integer roomId, String reservationEnd) {
        log.info("[updateReservationEnd] 요청됨 - roomId: {}, reservationEnd: {}", roomId, reservationEnd);


        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        // 예약 마감 날짜가 비어있으면 기본값 설정
        if (reservationEnd == null || reservationEnd.trim().isEmpty()) {
            log.error("[updateReservationEnd] reservationEnd 값이 없습니다!");
            throw new IllegalArgumentException("예약 마감 날짜가 필요합니다.");
        }

        room.setReservationEnd(reservationEnd);
        roomRepository.save(room);

        log.info("[updateReservationEnd] 객실 {}의 예약 마감 날짜가 {}로 업데이트됨", roomId, reservationEnd);
    }


    public void updateRoomStatusBasedOnReservationEnd(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        // 오늘 날짜와 비교하여 객실 상태 업데이트
        LocalDate today = LocalDate.now();
        LocalDate reservationEndDate = LocalDate.parse(room.getReservationEnd());

        if (reservationEndDate.isBefore(today)) {
            room.setRoomStatus(false);  // 예약 불가능 상태 (기간 만료)
            log.info("[updateRoomStatusBasedOnReservationEnd] 객실 {} 예약 마감됨", roomId);
        } else {
            room.setRoomStatus(true);  // 예약 가능 상태
            log.info("[updateRoomStatusBasedOnReservationEnd] 객실 {} 예약 가능", roomId);
        }

        roomRepository.save(room);
    }

    public void updateRoomWithoutImages(Integer roomId, RoomDTO roomDTO) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 아이디에 방을 찾을 수 없습니다: " + roomId));

        room.setRoomName(roomDTO.getRoomName());
        room.setRoomType(roomDTO.getRoomType());
        room.setRoomPrice(roomDTO.getRoomPrice());
        room.setRoomStatus(roomDTO.getRoomStatus());
        room.setRoomInfo(roomDTO.getRoomInfo());
        room.setReservationStart(roomDTO.getReservationStart());
        room.setReservationEnd(roomDTO.getReservationEnd());
        room.setStayStart(roomDTO.getStayStart());
        room.setStayEnd(roomDTO.getStayEnd());

        log.info("Room ID: {} 수정됨 (이미지는 변경되지 않음)", roomId);
        roomRepository.save(room);
    }

    /* 이미지 개수 체크 메서드*/
    public int countRoomImages(Integer roomId) {
        int imageCount = imageRepository.countByRoom_RoomId(roomId);
        log.info("Room ID: {} - 이미지 개수: {}", roomId, imageCount);
        return imageCount;
    }

    private Integer getLoggedInMemberId(Authentication authentication) {
        // authentication이 null이 아니고, 인증된 사용자가 있는지 확인
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("로그인된 사용자가 없습니다.");
        }
        // authentication.getName()을 memberName으로 대체
        String memberEmail = authentication.getName();

        // memberEmail이 null이거나 비어있을 경우 처리
        if (memberEmail == null || memberEmail.isEmpty()) {
            throw new RuntimeException("회원 정보가 존재하지 않습니다.");
        }

        // memberName을 통해 Member 조회
        Member member = memberService.findMemberByMemberEmail(memberEmail);

        // member가 null인 경우 처리
        if (member == null) {
            throw new RuntimeException("회원 정보가 존재하지 않습니다.");
        }

        return member.getMemberId(); // memberId 반환
    }
}
