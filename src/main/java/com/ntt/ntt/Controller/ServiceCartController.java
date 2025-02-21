package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.ServiceCartDetailDTO;
import com.ntt.ntt.DTO.ServiceCartItemDTO;
import com.ntt.ntt.DTO.ServiceCartOrderDTO;
import com.ntt.ntt.Service.ReservationService;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Service.ServiceCartItemService;
import com.ntt.ntt.Service.ServiceCartService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.IntersectionType;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ServiceCartController {
    private final ServiceCartService serviceCartService;
    private final ServiceCartItemService serviceCartItemService;
    private final ReservationService reservationService;
    private final RoomService roomService;

    //카트 컨트롤러 역시 데이터를받아 동적으로 만들것
    // 장바구니 조회
    @GetMapping("/api/cart")
    public ResponseEntity<List<ServiceCartDetailDTO>> getCart(Principal principal) {
        String memberEmail = principal.getName();
        List<ServiceCartDetailDTO> cartDetails = serviceCartService.listServiceCart(memberEmail);
        return new ResponseEntity<>(cartDetails, HttpStatus.OK);
    }

    // 장바구니 등록
    @PostMapping("/api/cart")
    public ResponseEntity<?> registerCart(@RequestBody @Valid ServiceCartItemDTO serviceCartItemDTO,
                                          BindingResult bindingResult,
                                          Principal principal) {
        // 유효성 검사 실패시
        if (bindingResult.hasErrors()) {
            StringBuffer sb = new StringBuffer();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage()).append("\n");
            }
            // 오류 메시지를 JSON 형식으로 반환
            return new ResponseEntity<>(new ErrorResponse(sb.toString()), HttpStatus.BAD_REQUEST);
        }

        // 로그인한 멤버의 이메일을 이용해 등록할 장바구니 아이템 추가
        String memberEmail = principal.getName();

        try {
            // 서비스 호출하여 장바구니 등록 처리
            Integer cartItemId = serviceCartService.registerServiceCart(serviceCartItemDTO, memberEmail);

            // 장바구니 등록 성공 시
            return new ResponseEntity<>(cartItemId, HttpStatus.CREATED);
        } catch (Exception e) {
            // 예외 발생 시 처리
            log.error("장바구니 등록 실패", e);
            return new ResponseEntity<>(new ErrorResponse("장바구니 등록 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 오류 응답 객체
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
    //장바구니 페이지를 만들 때 필요한 컨트롤러인데
    //이번에는 유저의 주문 페이지에 장바구니를 동적으로 만들었기에 필요하지 않을 듯 보임
    @GetMapping("/myPage/cart")
    public String orderHistory(Principal principal ,Model model) {
        List<ServiceCartDetailDTO> serviceCartDetailDTOList =
                serviceCartService.listServiceCart(principal.getName());

        //사용자에게 보여줄 장바구니 목록중에 CartDetailDTO(꼭 필요한 정보만 가공한 DTO)로 담은 lIST
        model.addAttribute("serviceCartDetailDTOList", serviceCartDetailDTOList);
        return "myPage/cart/history";
    }
    // 장바구니에 담긴 수량을 변경하는 기능
    @PutMapping("/api/updateCartItem")
    public ResponseEntity<?> updateCartItem(@RequestBody @Valid ServiceCartItemDTO serviceCartItemDTO,
                                            BindingResult bindingResult, Principal principal) {
        String memberEmail = principal.getName();

        log.info("수량변경을 위해서 넘어온 값 : " + serviceCartItemDTO);
        if (bindingResult.hasErrors()) {
            StringBuffer sb = new StringBuffer();
            List<FieldError> fieldErrorList = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrorList) {
                sb.append(fieldError.getDefaultMessage());
            }
            // 오류 메시지를 JSON 형식으로 반환 (Map 사용)
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errorMessage", sb.toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        try {
            // 카트 아이템의 수량을 변경
            serviceCartItemService.updateServiceCartItemCount(serviceCartItemDTO, memberEmail);

            // 성공적인 업데이트 후 응답 (Map 사용)
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "수량 업데이트 성공");
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        } catch (Exception e) {
            // 예외 처리 시 오류 메시지 JSON으로 반환
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errorMessage", "장바구니 수량변경이 잘못되었습니다. Q&A게시판으로 요청 바랍니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    //장바구니에 담긴 아이템을 삭제하는 기능
    @DeleteMapping("/api/cartItem/{serviceCartItemId}")
    public ResponseEntity deleteCartItem(@PathVariable("serviceCartItemId") Integer serviceCartItemId,
                                         Principal principal) {
        String memberEmail = principal.getName();
        //내 장바구니가 맞는지 확인
        if (!serviceCartService.validateServiceCartItem(serviceCartItemId, memberEmail)) {
            return new ResponseEntity<String>("장바구니 수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        //장바구니 아이템을 삭제
        serviceCartService.deleteServiceCartItem(serviceCartItemId);

        return new ResponseEntity<Integer>(serviceCartItemId,HttpStatus.OK);
    }

    //장바구니에 담긴 아이템을 주문내역 페이지로 보내는 주문기능을 담당
    @PostMapping("/api/cart/orders")
    public ResponseEntity<?> orderServiceCartItem(@RequestBody ServiceCartOrderDTO serviceCartOrderDTO,
                                               BindingResult bindingResult,
                                               Principal principal,
                                               Model model) {

        Integer reservationId = serviceCartOrderDTO.getReservationId();
        String memberEmail = principal.getName();
        log.info("컨트롤러로 들어온 serviceCartOrderDTO : " + serviceCartOrderDTO);
        log.info("예약정보로 알게된 reservaionId : " + reservationId);
        //리스트 형태로 serviceCartOderDTO를 담을건데
        // ServiceCartOrderDTO에는 자기 자신을 리스트 형태로 이미 담아놓는 메서드가 존재함
        List<ServiceCartOrderDTO> serviceCartOrderDTOList =
                serviceCartOrderDTO.getOrderDTOList();
        if (serviceCartOrderDTOList == null || serviceCartOrderDTOList.isEmpty()) {
            return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.FORBIDDEN);
        }

        for(ServiceCartOrderDTO serviceCartOrders : serviceCartOrderDTOList) {
            if (!serviceCartService.validateServiceCartItem(serviceCartOrders.getServiceCartItemId(), memberEmail)) {
                return new ResponseEntity<String>("주문권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }
        ReservationDTO reservation = new ReservationDTO();
        reservation.setReservationId(reservationId);

        Integer serviceOrderId = serviceCartService.orderServiceCartItem(serviceCartOrderDTOList, memberEmail, reservationId);

        model.addAttribute("reservationId", reservationId);
        return new ResponseEntity<Integer>(serviceOrderId, HttpStatus.OK);
    }

}
