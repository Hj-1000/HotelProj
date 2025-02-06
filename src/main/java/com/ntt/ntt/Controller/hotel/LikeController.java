package com.ntt.ntt.Controller.hotel;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntt.ntt.DTO.LikeDTO;
import com.ntt.ntt.Service.hotel.LikeService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;
    private final PaginationUtil paginationUtil;

    //장바구니목록
    @GetMapping("/list")
    public String likeList(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            String email = principal.getName();
            System.out.println("현재 로그인한 사용자 이메일: " + email); // 로그 추가

            model.addAttribute("likeDTO", likeService.likeList(email));

            System.out.println("likeDTO : " + likeService.likeList(email));

            return "myPage/like/list";

        } catch (Exception e) {
            e.printStackTrace(); // 예외 출력 (서버 로그에서 확인 가능)
            redirectAttributes.addFlashAttribute("error", "좋아요 목록을 불러오는 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/hotel/list";
        }
    }

    //장바구니 등록
    @ResponseBody
    @PostMapping("/register")
    public ResponseEntity<String> likeHotel(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody LikeDTO likeDTO) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        String memberEmail = userDetails.getUsername(); // 이메일 가져오기
        likeDTO.setMemberEmail(memberEmail); // LikeDTO에 이메일 설정

        // 이후 서비스 로직 실행
        likeService.likeRegister(likeDTO, memberEmail);  // 이메일을 서비스 메서드에 전달

        return ResponseEntity.ok("해당 호텔이 스크랩 되었습니다.");
    }


    //장바구니 제거
    @PostMapping("/delete")
    public ResponseEntity<?> likeDelete(@RequestBody Map<String, Integer> request) {
        log.info("받은 요청 데이터: " + request);

        Integer likeHotelId = request.get("likeHotelId");

        if (likeHotelId == null) {
            return new ResponseEntity<>("삭제할 즐겨찾기 ID가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        log.info("삭제할 likeHotelId: " + likeHotelId);

        likeService.delete(likeHotelId);

        return new ResponseEntity<>("즐겨찾기에서 삭제되었습니다.", HttpStatus.OK);
    }







}
