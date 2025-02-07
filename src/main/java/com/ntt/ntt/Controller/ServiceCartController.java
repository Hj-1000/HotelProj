package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.ServiceCartDetailDTO;
import com.ntt.ntt.DTO.ServiceCartItemDTO;
import com.ntt.ntt.DTO.ServiceCartOrderDTO;
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

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ServiceCartController {
    private final ServiceCartService serviceCartService;
    private final ServiceCartItemService serviceCartItemService;

    //카트 컨트롤러 역시 데이터를받아 동적으로 만들것

    //장바구니 등록
    @PostMapping("/api/cart")
    public ResponseEntity registerCart(@Valid ServiceCartItemDTO serviceCartItemDTO, BindingResult bindingResult, Principal principal) {
        log.info("view에서 넘어온 값 : " + serviceCartItemDTO);
        log.info("로그인 되었나?" + principal);
        // 유효성 검사
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
        // 값이 잘 넘어왓다면
        String memberEmail = principal.getName();

        Integer serviceCartItemId = null; //저장된 serviceCartItemId


        try {
            serviceCartItemId = serviceCartService.registerServiceCart(serviceCartItemDTO, memberEmail);
            //저장이 잘 되었다면 브라우저로 다시 전송
            return new ResponseEntity<Integer>(serviceCartItemId, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/myPage/cart")
    public String orderHistory(Principal principal ,Model model) {
        List<ServiceCartDetailDTO> serviceCartDetailDTOList =
                serviceCartService.listServiceCart(principal.getName());

        //사용자에게 보여줄 장바구니 목록중에 CartDetailDTO(꼭 필요한 정보만 가공한 DTO)로 담은 lIST
        model.addAttribute("serviceCartDetailDTOList", serviceCartDetailDTOList);
       return "myPage/cart/history";
    }

    @PostMapping("/api/cartItem")
    public ResponseEntity updateCartItem(@Valid ServiceCartItemDTO serviceCartItemDTO,
                                         BindingResult bindingResult, Principal principal) {
        String memberEmail = principal.getName();

        log.info("수량변경을 위해서 넘어온 값 : " + serviceCartItemDTO);
        if (bindingResult.hasErrors()) {
            StringBuffer sb = new StringBuffer();
            List<FieldError> fieldErrorList
                    = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrorList) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        //service에서 카트아이템 아이디를 통해서 카트아이템을 찾아온다
        //카트아이템의 수량을 현재 받은 cartItemDTO의 count로 변경
        try {
            serviceCartItemService.updateServiceCartItemCount(serviceCartItemDTO, memberEmail);
            return new ResponseEntity<String>(HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<String>("장바구니 수량변경이 잘못되었습니다. Q&A게시판으로 요청 바랍니다." , HttpStatus.BAD_REQUEST);
        }
    }

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

    @PostMapping("/api/cart/orders")
    public ResponseEntity orderServiceCartItem(@RequestBody ServiceCartOrderDTO serviceCartOrderDTO, BindingResult bindingResult,
                                               Principal principal) {
        String memberEmail = principal.getName();
        log.info("컨트롤러로 들어온 serviceCartOrderDTO" + serviceCartOrderDTO);
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
        Integer serviceOrderId = serviceCartService.orderServiceCartItem(serviceCartOrderDTOList, memberEmail);
        return new ResponseEntity<Integer>(serviceOrderId, HttpStatus.OK);
    }

}
