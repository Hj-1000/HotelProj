package com.ntt.ntt.Controller.hotel;

import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
@Log4j2
public class HotelController {

    private final HotelService hotelService;
    private final PaginationUtil paginationUtil;


    @GetMapping("/register")
    public String registerForm() {
        return "register";}

    @PostMapping("/register")
    public String registerProc(HotelDTO hotelDTO) {
        hotelService.hotelRegister(hotelDTO);
        return "redirect:/";
    }




}
