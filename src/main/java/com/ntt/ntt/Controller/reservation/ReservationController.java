package com.ntt.ntt.Controller.reservation;


import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.ReservationService;
import com.ntt.ntt.Service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/roomList")
@RequiredArgsConstructor
@Log4j2
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final MemberRepository memberRepository;
}
