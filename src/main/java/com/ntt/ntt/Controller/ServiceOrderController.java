package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.ServiceOrderDTO;
import com.ntt.ntt.DTO.ServiceOrderHistoryDTO;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Service.ServiceOrderService;
import com.ntt.ntt.Util.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;
    private final RoomService roomService; // roomId를 불러오기 위함
    private final PaginationUtil paginationUtil;
    //주문하기는 상품의 읽기 페이지에서
    //사용자가 볼 수 잇으므로, 따로 get으로 읽기 페이지는 만들지 않는다.
    //대신 그 페이지에서 보내주는 데이터를 바탕으로 orders orderItem에
    // 데이터를 입력하는 역할을 한다.

    //todo: roomId도 받게끔 수정했음 잘되는지 확인이 필요
    @PostMapping("/api/order")
    public ResponseEntity order(@Valid ServiceOrderDTO serviceOrderDTO, Integer roomId, BindingResult bindingResult, Principal principal) {
        //만약에 아이템 id가 없다면
        //만약에 수량이 없다면
        //유효성 검사
        log.info("주문컨트롤러로 들어온 roomId" + roomId);
        log.info("컨트롤러로 들어온 serviceOrderDTO: " + serviceOrderDTO);
        if (bindingResult.hasErrors()) {
            StringBuffer sb = new StringBuffer();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                log.info("필드 : " + fieldError.getField() + "메세지 : " + fieldError.getDefaultMessage());
                sb.append(fieldError.getDefaultMessage());
            }
            log.info(sb.toString());
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        //주문은 로그인한 사용자 물론 판매자 또한 다른 사람이 만든 아이템, 자기 아이템을 살 수 있음
        //먼저 저장을 한다
        Integer result =
                serviceOrderService.createOrder(serviceOrderDTO, principal.getName(), roomId);

        log.info("result: " + result);

        if (result == null) {
            return new ResponseEntity<String>("주문 수량이 판매 수량보다 많습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<String>("주문완료", HttpStatus.OK);
    }

    //이건 유저가 볼 유저의 주문내역 페이지 호출함수

    @GetMapping({"/myPage/order/history", "/myPage/order/history/{page}"})
    public String orderHistory(@PathVariable("page") Optional<Integer> page,
                               Principal principal, Model model) {
        if (principal == null) {
            log.info("로그인을 하지않은 접속오류");
            return "redirect:/login";
        }

        // 페이지 번호가 없으면 0을 기본값으로 설정
        int currentPage = page.orElse(0);
        Pageable pageable = PageRequest.of(currentPage, 4);
        log.info(pageable.toString());

        String memberEmail = principal.getName();

        Page<ServiceOrderHistoryDTO> serviceOrderHistoryDTOPage =
                serviceOrderService.getOrderList(memberEmail, pageable);

        model.addAttribute("orders", serviceOrderHistoryDTOPage);
        // HTML에서 getContent()를 호출할 것임
        model.addAttribute("page", currentPage);
        model.addAttribute("maxPage", 5);
        return "myPage/order/history";
    }

    //주문취소 api
    @PostMapping("/api/order/{serviceOrderId}/cancel")
    public ResponseEntity cnacelOrder(
            @PathVariable("serviceOrderId") Integer serviceOrderId, Principal principal) {
        //받아올 serviceOrderId 는 취소할 pk이다.
        //serviceOrderId를 삭제하고 , serviceOrderItem에서
        // serviceOrderId를 참조하고 있는 serviceOrderItem을 찾아 삭제한다.
        // 단방향 처리 : orderItem을 먼저 삭제(자식 먼저 삭제) 하고
        // orderId를 삭제. 이미 양방향으로 만들었음
        log.info("취소할 주문번호" + serviceOrderId);
        log.info("최소할 주문번호로 달린 아이템들");
        if (!serviceOrderService.validateOrder(serviceOrderId, principal.getName())) {
            //내 제품이 아니라면
            return new ResponseEntity<String>("해당 주문에 대한 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);

        }
        //취소를 한다. orderStatus를 CANCELED로 바꾸고, 주문했던 아이템들의 수량도 돌려놓는다.
        //주문에 담긴 주문아이템들은 데이터를 가지고 있다.
        //위 기능은 serviceOrderService의 cancelOrder에 구현해놓았음

        serviceOrderService.cancelOrder(serviceOrderId);

        return new ResponseEntity<Integer>(serviceOrderId, HttpStatus.OK);
    }
}
