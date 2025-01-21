package com.ntt.ntt.Controller;


import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/manager/room")
@RequiredArgsConstructor
@Log4j2
public class RoomController {

    private final RoomService roomService;


    // 1. Room 등록 페이지로 이동
    @GetMapping("/register")
    public String registerRoomForm(Model model) {
        //빈 RoomDTO 전달
        model.addAttribute("room", new RoomDTO());
        
        //register.htm로 이동
        return "manager/room/register";
    }
    
    // Room 등록
    @PostMapping("/register")
    public String registerRoomProc(@ModelAttribute RoomDTO roomDTO) {
        //Room 등록
        roomService.registerRoom(roomDTO);

        //등록후 list 페이지로 이동하기
        return "redirect:/manager/room/list";
}

    // 2. 모든 객실 조회
    @GetMapping("/list")
    public String AllRoomsForm(@PageableDefault(size = 5, page = 0) Pageable pageable, Model model) {
        // Room 데이터를 페이지네이션으로 가져오기
        Page<RoomDTO> roomDTOS = roomService.a(pageable);


        // 왜 안나와
        log.info("RoomDTOs Content: {}", roomDTOS.getContent());
        log.info("Total Elements: {}", roomDTOS.getTotalElements());
        log.info("Total Pages: {}", roomDTOS.getTotalPages());

        // 페이지네이션 정보를 생성
        Map<String, Integer> pageInfo = PaginationUtil.pagination(roomDTOS);

        // Thymeleaf에서 1부터 시작하도록 currentPage 조정
        pageInfo.put("currentPage", pageable.getPageNumber() + 1); // 0부터 시작하는 번호를 1로 조정
        pageInfo.put("prevPage", Math.max(1, pageable.getPageNumber())); // 이전 페이지
        pageInfo.put("nextPage", Math.min(roomDTOS.getTotalPages(), pageable.getPageNumber() + 2)); // 다음 페이지


        // 모델에 데이터 추가
        model.addAttribute("list", roomDTOS); // 페이징된 RoomDTO 리스트
        model.addAllAttributes(pageInfo); // 페이지 정보 (startPage, endPage, prevPage 등)

        // 가격 포맷팅 추가
        for (RoomDTO room : roomDTOS) {
            String formattedPrice = String.format("%,d", room.getRoomPrice());
            room.setFormattedRoomPrice(formattedPrice); // RoomDTO에 추가된 필드 사용
        }

        return "manager/room/list"; // 페이지 이동
    }

    // 특정 룸 조회
    @GetMapping("/{roomId}")
    public String getRoomDetailsForm(@PathVariable Integer roomId, Model model) {
        RoomDTO room = roomService.readRoom(roomId);
        String formattedPrice = String.format("%,d KRW", room.getRoomPrice());
        model.addAttribute("room", room);
        model.addAttribute("formattedPrice", formattedPrice);
        return "manager/room/read";
    }

    // 3. Room 수정 페이지로 이동
    @GetMapping("/update/{roomId}")
    public String updateRoomForm(@PathVariable Integer roomId, Model model) {
        // 수정할 Room 데이터 가져오기
        RoomDTO room = roomService.readRoom(roomId);
        // Model에 데이터 추가
        model.addAttribute("room", room);

        // update.html로 이동
        return "manager/room/update";
    }

    // Room 수정
    @PostMapping("/update/{roomId}")
    public String updateRoomProc(@PathVariable Integer roomId, @ModelAttribute RoomDTO roomDTO) {
        //Room 수정
        roomService.updateRoom(roomId, roomDTO);
        // 수정 후 list 페이지로 이동
        return "redirect:/manager/room/list";
    }


    // 4. Room 삭제
    @GetMapping("/delete/{roomId}")
    public String deleteRoomForm(@PathVariable Integer roomId) {
        // Room 삭제
        roomService.deleteRoom(roomId);

        // Room 삭제 후 list 페이지로 이동
        return "redirect:/manager/room/list";
    }
}
