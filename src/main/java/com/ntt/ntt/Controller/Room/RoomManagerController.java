package com.ntt.ntt.Controller.Room;


import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/room")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "RoomManagerController", description = "관리자 객실 관리 컨트롤러")
public class RoomManagerController {

    private final RoomService roomService;
    private final ReservationRepository reservationRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final MemberService memberService;

    /* -----------관리자 페이지----------- */

    @Operation(summary = "객실 등록 페이지", description = "관리자가 객실을 등록할 수 있는 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerRoomForm(Model model, Authentication authentication) {
        //빈 RoomDTO 전달
        model.addAttribute("room", new RoomDTO());

        //hotelDTO hotelName 전달하기
        List<HotelDTO> hotelDTOS = roomService.getAllHotel(authentication);
        model.addAttribute("hotelDTOS", hotelDTOS);
        model.addAttribute("hotelDTO", new HotelDTO());

        //register.htm로 이동
        return "manager/room/register";
    }

    @Operation(summary = "객실 등록", description = "관리자가 새로운 객실을 등록한다. 객실 이미지와 배너 이미지는 필수이다.")
    @PostMapping("/register")
    public String registerRoomProc(@ModelAttribute RoomDTO roomDTO,
                                   @Valid BindingResult bindingResult,
                                   @RequestParam("imageFile") List<MultipartFile> imageFile,
                                   @RequestParam("bannerImageFile") MultipartFile bannerImageFile,
                                   @RequestParam("imageTitles") List<String> imageTitles,
                                   @RequestParam("imageDescriptions") List<String> imageDescriptions,
                                   RedirectAttributes redirectAttributes) {

        log.info("객실 등록 요청: {}", roomDTO);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "지사를 선택해야 합니다.");
            return "redirect:/manager/room/register";
        }

        if (imageFile == null || imageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "객실 이미지는 필수입니다.");
            return "redirect:/manager/room/register";
        }

        if (bannerImageFile == null || bannerImageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "배너(대표) 이미지는 필수입니다.");
            return "redirect:/manager/room/register";
        }

        roomService.registerRoom(roomDTO, imageFile, bannerImageFile, imageTitles, imageDescriptions);

        redirectAttributes.addFlashAttribute("successMessage", "객실이 성공적으로 등록되었습니다.");
        return "redirect:/manager/room/list";
    }

    @Operation(summary = "객실 목록 조회", description = "관리자가 모든 객실을 조회할 수 있는 페이지를 반환한다.")
    @GetMapping("/list")
    public String listForm(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            Authentication authentication,
            Model model, RedirectAttributes redirectAttributes) {

        if ("roomStatus".equals(category) && keyword != null) {
            if ("av".equalsIgnoreCase(keyword)) {
                keyword = "Available";
            } else if ("un".equalsIgnoreCase(keyword)) {
                keyword = "Unavailable";
            }
        }

        // 현재 페이지 정보를 Pageable에 반영
        Pageable updatedPageable = PageRequest.of(page, pageable.getPageSize());

        // 검색 조건과 페이징 정보를 이용하여 데이터 가져오기
        Page<RoomDTO> roomDTOS;

        try {
            // 2. 검색 수행
            roomDTOS = roomService.listRooms(keyword, category, updatedPageable, authentication);
        } catch (IllegalArgumentException e) {
            log.warn(" 검색 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "검색 조건이 올바르지 않습니다.");
            return "redirect:/manager/room/list";
        }

        // 상태 자동 업데이트: 예약 마감일이 지난 경우 예약 불가 처리
        LocalDate today = LocalDate.now();
        roomDTOS.forEach(room -> {
            if (room.getReservationEnd() != null) {
                LocalDate reservationEndDate = LocalDate.parse(room.getReservationEnd());
                room.setRoomStatus(!reservationEndDate.isBefore(today));
            }
        });


        // 유효성 검사: 상태(category가 roomStatus일 경우)
        if ("roomStatus".equals(category)) {
            if (!"Available".equalsIgnoreCase(keyword) && !"Unavailable".equalsIgnoreCase(keyword)) {
                log.warn("Invalid room status keyword: {}", keyword);
                model.addAttribute("errorMessage", "유효하지 않은 텍스트입니다. 'Available' or 'Unavailable' 입력해주세요.");
                return "manager/room/list"; // 에러 메시지를 추가한 리스트 페이지로 리다이렉트
            }
        }

        // 로그로 이미지 확인
        for (RoomDTO room : roomDTOS) {
            if (room.getRoomImageDTOList() != null && !room.getRoomImageDTOList().isEmpty()) {
                log.info("Room ID: {} - 이미지 개수: {}", room.getRoomId(), room.getRoomImageDTOList().size());
            } else {
                log.info("Room ID: {} - 이미지 없음", room.getRoomId());
            }
        }

        // 페이지네이션 정보 생성
        Map<String, Integer> pageInfo = PaginationUtil.pagination(roomDTOS);

        // 전체 페이지 수
        int totalPages = roomDTOS.getTotalPages();

        // 현재 페이지 번호
        int currentPage = pageInfo.get("currentPage");

        // 시작 페이지와 끝 페이지 계산 (현재 페이지를 기준으로 최대 10페이지까지)
        int startPage = Math.max(1, currentPage - 4); // 10개씩 끊어서 시작 페이지 계산
        int endPage = Math.min(startPage + 9, totalPages); // 최대 10페이지까지, 전체 페이지 수를 넘지 않도록

        // 페이지 정보 업데이트
        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);


        // 모델에 데이터 추가
        model.addAttribute("currentPage", page);
        model.addAttribute("list", roomDTOS); // 페이징된 RoomDTO 리스트
        model.addAttribute("keyword", keyword); // 검색 키워드 전달
        model.addAttribute("category", category); // 검색 카테고리 전달
        model.addAllAttributes(pageInfo); // 페이지 정보 추가

        // 가격 포맷팅
        for (RoomDTO room : roomDTOS) {
            if (room.getFormattedRoomPrice() == null) {
                String formattedPrice = String.format("%,d", room.getRoomPrice());
                room.setFormattedRoomPrice(formattedPrice);
            }
        }

        return "manager/room/list";
    }

    @Operation(summary = "객실 상세 조회", description = "특정 객실의 상세 정보를 조회한다.")
    @GetMapping("/{roomId}")
    public String readRoomForm(@PathVariable Integer roomId, Model model) {
        log.info("Fetching details for roomId: {}", roomId);

        try {
            RoomDTO room = roomService.readRoom(roomId);
            if (room == null) {
                throw new IllegalArgumentException("등록된 아이디에 방을 찾을 수 없습니다: " + roomId);
            }

            String formattedPrice = String.format("%,d KRW", room.getRoomPrice());
            model.addAttribute("room", room);
            model.addAttribute("formattedPrice", formattedPrice);

            return "manager/room/read";
        } catch (IllegalArgumentException e) {
            log.warn("객실을 찾을 수 없습니다. Room ID: {}", roomId);
            model.addAttribute("errorMessage", "요청하신 객실을 찾을 수 없습니다.");
            return "error/404"; // 404 페이지로 이동
        }
    }

    @Operation(summary = "객실 수정 페이지", description = "관리자가 객실 정보를 수정할 수 있는 페이지를 반환한다.")
    @GetMapping("/update/{roomId}")
    public String updateRoomForm(@PathVariable Integer roomId, Model model) {
        RoomDTO room = roomService.readRoom(roomId);

        // 이미지 정보 디버깅 출력
        if (room.getRoomImageDTOList() != null) {
            for (ImageDTO imageDTO : room.getRoomImageDTOList()) {
                log.info("이미지 정보 - ID: {}, 제목: {}, 설명: {}",
                        imageDTO.getImageId(), imageDTO.getImageTitle(), imageDTO.getImageDescription());
            }
        } else {
            log.warn("roomImageDTOList가 비어 있음");
        }

        // 방의 예약 여부를 reserved 필드로 설정
        room.setReserved(reservationRepository.existsByRoom_RoomId(roomId));

        model.addAttribute("room", room);

        return "manager/room/update";
    }

    @Operation(summary = "객실 정보 수정", description = "관리자가 기존 객실 정보를 수정한다.")
    @PostMapping("/update/{roomId}")
    public String updateRoomProc(@PathVariable Integer roomId,
                                 @ModelAttribute RoomDTO roomDTO,
                                 @RequestParam(value = "imageFile", required = false) List<MultipartFile> imageFile,
                                 @RequestParam(value = "bannerImageFile", required = false) MultipartFile bannerImageFile,
                                 @RequestParam(value = "newImageTitles", required = false) List<String> newImageTitles,
                                 @RequestParam(value = "newImageDescriptions", required = false) List<String> newImageDescriptions,
                                 @RequestParam(value = "existingImageIds", required = false) List<Integer> existingImageIds,
                                 @RequestParam(value = "existingImageTitles", required = false) List<String> existingImageTitles,
                                 @RequestParam(value = "existingImageDescriptions", required = false) List<String> existingImageDescriptions,
                                 @RequestParam(value = "deleteBannerImage", required = false, defaultValue = "false") boolean deleteBannerImage,
                                 @RequestParam(value = "deleteImages", required = false) List<String> deleteImagesStr,
                                 RedirectAttributes redirectAttributes) {


        // List가 null이면 빈 리스트로 초기화
        if (newImageTitles == null) newImageTitles = new ArrayList<>();
        if (newImageDescriptions == null) newImageDescriptions = new ArrayList<>();
        if (existingImageIds == null) existingImageIds = new ArrayList<>();
        if (existingImageTitles == null) existingImageTitles = new ArrayList<>();
        if (existingImageDescriptions == null) existingImageDescriptions = new ArrayList<>();
        if (deleteImagesStr == null) deleteImagesStr = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate newReservationEnd = roomDTO.getReservationEnd() != null ? LocalDate.parse(roomDTO.getReservationEnd()) : null;

        if (newReservationEnd != null && newReservationEnd.isBefore(today)) {
            roomDTO.setRoomStatus(false); // 예약 불가
        } else {
            roomDTO.setRoomStatus(true); // 예약 가능
        }

        // 기존 이미지 제목과 설명이 비어있는 경우 예외 처리
        for (int i = 0; i < existingImageIds.size(); i++) {
            String title = (existingImageTitles.size() > i) ? existingImageTitles.get(i) : "";
            String description = (existingImageDescriptions.size() > i) ? existingImageDescriptions.get(i) : "";

            if (title.trim().isEmpty() || description.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "기존 이미지의 제목과 설명을 모두 입력해야 합니다.");
                return "redirect:/manager/room/update/" + roomId;
            }
        }

        // 기존 이미지 제목 & 설명 업데이트
        if (existingImageIds != null && !existingImageIds.isEmpty()) {
            for (int i = 0; i < existingImageIds.size(); i++) {
                // 제목과 설명이 비어있는 경우 기본값 설정
                String title = (i < existingImageTitles.size() && !existingImageTitles.get(i).trim().isEmpty()) ? existingImageTitles.get(i) : "제목 없음";
                String description = (i < existingImageDescriptions.size() && !existingImageDescriptions.get(i).trim().isEmpty()) ? existingImageDescriptions.get(i) : "설명 없음";

                existingImageTitles.set(i, title);
                existingImageDescriptions.set(i, description);
            }
            roomService.updateRoomImageDetails(existingImageIds, existingImageTitles, existingImageDescriptions);
        }

        // 기존 배너 이미지 삭제
        if (deleteBannerImage || (bannerImageFile != null && !bannerImageFile.isEmpty())) {
            roomService.updateRoomBannerImage(roomId, bannerImageFile);
        }

        // 문자열 리스트 → 정수 리스트 변환
        List<Integer> deleteImages = new ArrayList<>();
        if (deleteImagesStr != null && !deleteImagesStr.isEmpty()) {
            try {
                deleteImages = deleteImagesStr.stream()
                        .flatMap(str -> Arrays.stream(str.split(",")))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .distinct()
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                log.warn("삭제할 이미지 ID 변환 오류: {}", deleteImagesStr, e);
                redirectAttributes.addFlashAttribute("errorMessage", "이미지 ID 변환 중 오류 발생");
                return "redirect:/manager/room/update/" + roomId;
            }
        }

        // 기존 이미지 삭제 로직
        if (!deleteImages.isEmpty()) {
            for (Integer imageId : deleteImages) {
                if (imageRepository.existsById(imageId)) {
                    try {
                        imageService.delImage(imageId);
                        log.info("삭제된 이미지 ID: {}", imageId);
                    } catch (Exception e) {
                        log.warn("이미지를 삭제하는 중 오류 발생. ID: {}", imageId, e);
                    }
                } else {
                    log.warn("이미지를 찾을 수 없습니다. ID: {}", imageId);
                }
            }
        }

        int remainingImages = imageRepository.countByRoom_RoomId(roomId);
        log.info("남아있는 이미지 개수: {}", remainingImages);
        boolean hasNewImages = (imageFile != null && !imageFile.isEmpty());

        // 모든 이미지 삭제 시 예외 처리
        if (remainingImages == 0 && !hasNewImages) {
            redirectAttributes.addFlashAttribute("errorMessage", "객실 이미지는 최소 1개 이상 등록해야 합니다.");
            return "redirect:/manager/room/update/" + roomId;
        }

        if (hasNewImages) {
            log.info("새로운 이미지 저장 시작");

            List<ImageDTO> imageDTOList = new ArrayList<>();
            for (int i = 0; i < imageFile.size(); i++) {
                ImageDTO imageDTO = new ImageDTO();

                // 새로운 이미지 제목과 설명 적용
                String title = (i < newImageTitles.size() && !newImageTitles.get(i).trim().isEmpty()) ? newImageTitles.get(i) : "제목 없음";
                String description = (i < newImageDescriptions.size() && !newImageDescriptions.get(i).trim().isEmpty()) ? newImageDescriptions.get(i) : "설명 없음";

                imageDTO.setImageTitle(title);
                imageDTO.setImageDescription(description);
                imageDTOList.add(imageDTO);
            }

            roomDTO.setRoomImageDTOList(imageDTOList);
            roomService.updateRoom(roomId, roomDTO, imageFile, newImageTitles, newImageDescriptions, deleteImages, existingImageIds, existingImageTitles, existingImageDescriptions);
        } else {
            log.info("이미지를 변경하지 않고 방 정보만 업데이트");

            roomDTO.setRoomImageDTOList(new ArrayList<>());
            for (int i = 0; i < existingImageIds.size(); i++) {
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setImageId(existingImageIds.get(i));
                imageDTO.setImageTitle(i < existingImageTitles.size() ? existingImageTitles.get(i) : "");
                imageDTO.setImageDescription(i < existingImageDescriptions.size() ? existingImageDescriptions.get(i) : "");
                roomDTO.getRoomImageDTOList().add(imageDTO);
            }

            roomService.updateRoomWithoutImages(roomId, roomDTO);
        }

        redirectAttributes.addFlashAttribute("successMessage", "객실 정보가 성공적으로 수정되었습니다.");
        return "redirect:/manager/room/list";
    }

    @Operation(summary = "객실 삭제", description = "관리자가 특정 객실을 삭제한다.")
    @GetMapping("/delete/{roomId}")
    public String deleteRoomForm(@PathVariable Integer roomId, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(roomId);
            redirectAttributes.addFlashAttribute("successMessage", "삭제가 완료되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage",  e.getMessage());
        }
        return "redirect:/manager/room/list";
    }

}