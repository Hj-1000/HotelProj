package com.ntt.ntt.Controller.hotel;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntt.ntt.DTO.LikeDTO;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Likes;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.hotel.LikeHotelRepository;
import com.ntt.ntt.Repository.hotel.LikeRepository;
import com.ntt.ntt.Service.hotel.LikeService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/like")
@Tag(name = "LikeController", description = "유저가 보는 호텔 스크랩")
public class LikeController {

    private final LikeService likeService;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final LikeHotelRepository likeHotelRepository;

    //장바구니목록
    @Operation(summary = "사용자 호텔 즐겨찾기 목록", description = "호텔 즐겨찾기 목록 페이지로 이동한다.")
    @GetMapping("/list")
    public String likeList(Model model, Principal principal,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(defaultValue = "0") int page,  // 페이지 번호 (기본값: 0)
                           @RequestParam(defaultValue = "6") int size)  {
        try {
            String email = principal.getName();
            System.out.println("현재 로그인한 사용자 이메일: " + email); // 로그 추가

            // Pageable 객체 생성 (페이지 번호와 페이지 크기)
            Pageable pageable = PageRequest.of(page, size);

            // 페이징 처리된 좋아요 목록을 서비스에서 가져오기
            Page<LikeDTO> likeDTOPage = likeService.likeList(email, pageable);

            System.out.println("likeDTO : " + likeDTOPage);

            // 모델에 페이징 처리된 데이터와 페이지 정보 추가
            model.addAttribute("likeDTO", likeDTOPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", likeDTOPage.getTotalPages());
            model.addAttribute("totalItems", likeDTOPage.getTotalElements());
            model.addAttribute("pageSize", size);

            // 페이지 렌더링
            return "myPage/like/list";

        } catch (Exception e) {
            e.printStackTrace(); // 예외 출력 (서버 로그에서 확인 가능)
            redirectAttributes.addFlashAttribute("error", "좋아요 목록을 불러오는 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/hotel/list";
        }
    }



    //즐겨찾기 등록
    @Operation(summary = "사용자 즐겨찾기 등록", description = "원하는 호텔을 즐겨찾기에 등록한다.")
    @ResponseBody
    @PostMapping("/register")
    public ResponseEntity<String> likeHotel(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody LikeDTO likeDTO) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String memberEmail = userDetails.getUsername(); // 현재 로그인한 사용자의 이메일
        likeDTO.setMemberEmail(memberEmail); // DTO에 이메일 설정

        String resultMessage = likeService.likeRegister(likeDTO, memberEmail);

        return ResponseEntity.ok(resultMessage);  // 성공/실패 메시지를 반환
    }

    //해당 호텔이 이미 담겨있나 확인하는 API -> 버튼 변경을 위해
    @Operation(summary = "즐겨찾기 유무 체크", description = "해당 호텔을 즐겨찾기에 등록되었는지 확인한다.")
    @GetMapping("/check/{hotelId}")
    public ResponseEntity<Boolean> checkIfLiked(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable Integer hotelId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        String memberEmail = userDetails.getUsername();
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 회원이 존재하지 않습니다."));

        Likes likes = likeRepository.findByMember_MemberEmail(memberEmail);
        if (likes == null) {
            return ResponseEntity.ok(false);  // 장바구니 자체가 없으면 스크랩 X
        }

        // 해당 회원의 장바구니에 해당 호텔이 있는지 확인
        boolean isLiked = likeHotelRepository.findByLikesAndHotel(likes, new Hotel(hotelId)).isPresent();

        return ResponseEntity.ok(isLiked);
    }



//    @ResponseBody
//    @PostMapping("/register")
//    public ResponseEntity<String> likeHotel(@AuthenticationPrincipal UserDetails userDetails,
//                                            @RequestBody LikeDTO likeDTO) {
//        if (userDetails == null) {
//            throw new IllegalArgumentException("로그인이 필요합니다.");
//        }
//
//        String memberEmail = userDetails.getUsername(); // 이메일 가져오기
//        likeDTO.setMemberEmail(memberEmail); // LikeDTO에 이메일 설정
//
//        // 이후 서비스 로직 실행
//        likeService.likeRegister(likeDTO, memberEmail);  // 이메일을 서비스 메서드에 전달
//
//        return ResponseEntity.ok("해당 호텔이 스크랩 되었습니다.");
//    }


    //즐겨찾기 제거
    @Operation(summary = "사용자 즐겨찾기 삭제", description = "해당하는 호텔을 즐겨찾기에서 제거한다.")
    @PostMapping("/delete")
    public ResponseEntity<?> likeDelete(@RequestBody Map<String, Integer> request) {
        Integer hotelId = request.get("hotelId");

        if (hotelId == null) {
            return new ResponseEntity<>("삭제할 호텔 ID가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memberEmail = authentication.getName(); // 로그인한 사용자 이메일 가져오기

        // 회원 ID 조회
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 회원이 존재하지 않습니다."));
        Integer memberId = member.getMemberId(); // 회원 ID 가져오기

        likeService.deleteByHotelIdAndMemberId(hotelId, memberId);

        return new ResponseEntity<>("즐겨찾기에서 삭제되었습니다.", HttpStatus.OK);
    }


    @PostMapping("/deleteList")
    public ResponseEntity<?> likeDeleteList(@RequestBody Map<String, Integer> request) {
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
