package com.ntt.ntt.Controller.hotel;


import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.LikeDetailDTO;
import com.ntt.ntt.DTO.LikeHotelDTO;
import com.ntt.ntt.Service.hotel.LikeService;
import com.ntt.ntt.Util.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping
public class LikeController {

    private final LikeService likeService;
    private final PaginationUtil paginationUtil;

    @PostMapping("/likes")
    public @ResponseBody ResponseEntity order(
            @RequestBody @Valid LikeHotelDTO likeHotelDTO,
            BindingResult bindingResult, Principal principal, Model model) {


        System.out.println("즐겨찾기 - 입력받은 호텔아이디: " + likeHotelDTO.getHotelId());

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }//Valid수행하여 조건에 부합하지 않을경우 에러 필드와
            //에러메시지가 담긴다. dto 에 선언해놓음

            return new ResponseEntity<String>
                    (sb.toString(), HttpStatus.BAD_REQUEST);

        }

        String email = principal.getName(); //로그인한 사람의 이메일
        Integer likeHotelId;

        // 서비스를 통해서 즐겨찾기에 아이템을 담는다. addCart(likeHotelDTO, email);
        // likeHotelDTO에 itemId로 아이템을 findbyID로 객체를 가져와서 참조하도록

        try {
            likeHotelId = likeService.addLikeHotel(likeHotelDTO, email);

        }catch (Exception e){
            return new ResponseEntity<String>
                    (e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Integer>( likeHotelId, HttpStatus.OK);

    }

    //목록
    @GetMapping("/likes")
    public String likeList(@RequestParam(defaultValue = "1") int page,
                           @PageableDefault(size = 9) Pageable pageable,
                           Principal principal, Model model) {


        // 페이지 번호를 0-based로 변환
        Pageable adjustedPageable = PageRequest.of(page - 1, pageable.getPageSize());

        Page<LikeDetailDTO> likeDetailDTOList = likeService.likeList(principal.getName(), adjustedPageable);

        // 페이지 정보 계산
        Map<String, Integer> pageInfo = paginationUtil.pagination(likeDetailDTOList);

        // 전체 페이지 수
        int totalPages = likeDetailDTOList.getTotalPages();
        int currentPage = pageInfo.get("currentPage");

        // 페이지 정보 업데이트
        int startPage = Math.max(1, currentPage - 4);
        int endPage = Math.min(startPage + 9, totalPages);

        int prevPage = Math.max(1, currentPage - 1);
        int nextPage = Math.min(totalPages, currentPage + 1);
        int lastPage = totalPages;

        // 페이지 정보에 추가
        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);
        pageInfo.put("prevPage", prevPage);
        pageInfo.put("nextPage", nextPage);
        pageInfo.put("lastPage", lastPage);

        //dtoList 로그
        System.out.println("목록 보자~ :"+likeDetailDTOList);

        // 모델에 데이터 추가
        model.addAttribute("likeDetailDTOList", likeDetailDTOList);
        model.addAttribute("pageInfo", pageInfo);

        return "/myPage/like/list";
    }



    @DeleteMapping("/likeHotel/{likeHotelId}")
    public @ResponseBody ResponseEntity deletelikeHotel(
            @PathVariable("likeHotelId") Integer likeHotelId,
            Principal principal){

        log.info("들어온 아이디: "+ likeHotelId);

//        System.out.println("넘어온 likeHotelId : " + likeHotelId);
        //넘어온값이 확인되면 저 값을 이용해서
        //아까 확인한대로 service에서 id를 넘겨주고 그 아이디를 통해서
        // repository로 id를 넘겨서 deletebyid(Integer id) 메소드를 통해서 삭제
        // id를 넘겨서 entity를 찾아오고 예외처리한후 entity를 가지고 delete(entity)수행
        if (principal != null) {
            System.out.println("로그인한사람 : " + principal.getName());

        }       // service.validate 에서 이메일과 즐겨찾기의 호텔의 만든이의 t/f 관계 반대
        if(!likeService.validateLikeHotel(likeHotelId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        String likeHotelNm = likeService.deleteLikeHotel(likeHotelId);


        //return new ResponseEntity<Integer>(likeHotelId, HttpStatus.OK);
        return new ResponseEntity<String>(likeHotelNm, HttpStatus.OK);
    }


    //"/likeHotel/" + likeHotelId  + "?count=" + count;
    @PutMapping("/likeHotel/{likeHotelId}")
    public @ResponseBody ResponseEntity updateLikeHotel(
            @PathVariable("likeHotelId") Integer likeHotelId,
            int count, Principal principal){

        //url로 받은 파라미터 likeHotelId와
        //쿼리스트링으로 받은 count를 이용하여
        // db에서 likeHotel을 찾아서 수량을 변경 수행

        if(count <= 0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        }else if( !likeService.validateLikeHotel(likeHotelId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Integer>(likeHotelId, HttpStatus.OK);
    }

}
