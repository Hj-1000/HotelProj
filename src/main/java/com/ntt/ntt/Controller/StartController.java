package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StartController {

    private final MemberService memberService;

    @GetMapping("/admin/memberList")
    public String getAllMembers(Model model) {
        try {
            List<MemberDTO> memberDTOList = memberService.getAllMembers();
            if (memberDTOList == null || memberDTOList.isEmpty()) {
                model.addAttribute("message", "회원 목록이 없습니다.");
            } else {
                model.addAttribute("memberDTOList", memberDTOList);
            }
            System.out.println("전달된 회원 수: " + memberDTOList.size()); // 디버깅용 로그
        } catch (Exception e) {
            model.addAttribute("error", "회원 목록을 가져오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return "admin/memberList";
    }

    @GetMapping("/admin/executive")
    public String a(){
        return "admin/executive";
    }

    @GetMapping("/admin/executiveRegister")
    public String b(){
        return "admin/executiveRegister";
    }

    @GetMapping("/admin/hotelHeadquarters")
    public String c(){
        return "admin/hotelHeadquarters";
    }

    @GetMapping("/admin/hotelHeadquartersRegister")
    public String d(){
        return "admin/hotelHeadquartersRegister";
    }

    @GetMapping("/roomList")
    public String e(){
        return "roomList";
    }
}
