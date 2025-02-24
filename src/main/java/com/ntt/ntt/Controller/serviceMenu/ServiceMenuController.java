package com.ntt.ntt.Controller.serviceMenu;

import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Service.ServiceCateService;
import com.ntt.ntt.Service.ServiceMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
@Tag(name = "ServiceMenuController", description = "유저가 보는 메뉴 목록창")
public class ServiceMenuController {
    private final ServiceMenuService serviceMenuService;
    private final ServiceCateService serviceCateService;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    @Operation(summary = "유저의 메뉴 주문 페이지", description = "등록되어 있는 메뉴를 조회한다.")
    @GetMapping("/myPage/menu/list")
    public String list(@RequestParam(required = false) Integer reservationId,  // 예약 ID 파라미터
                       @RequestParam(required = false) Integer roomId,
                       Model model,
                       Principal principal) {
        if (principal == null || principal.getName() == null) {
            // 로그인을 하지 않으면 로그인 페이지로 이동
            return "redirect:/login";
        }

        Integer hotelId = null;

        if (roomId != null) {
            RoomDTO roomDTO = roomService.readRoom(roomId);
            hotelId = roomDTO.getHotelId().getHotelId();
        }

        model.addAttribute("reservationId", reservationId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("hotelId", hotelId);

        List<ServiceMenuDTO> serviceMenuDTOS = serviceMenuService.listUserMenu();
        List<ServiceCateDTO> serviceCateDTOS = serviceCateService.getAllServiceCate();
        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
        model.addAttribute("serviceMenuDTOS", serviceMenuDTOS);

        return "/myPage/menu/list";
    }

}
