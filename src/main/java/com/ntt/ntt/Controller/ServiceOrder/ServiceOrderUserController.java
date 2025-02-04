package com.ntt.ntt.Controller.ServiceOrder;

import com.ntt.ntt.Constant.ServiceMenuStatus;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.DTO.ServiceOrderDTO;
import com.ntt.ntt.Service.ServiceCateService;
import com.ntt.ntt.Service.ServiceMenuService;
import com.ntt.ntt.Service.ServiceOrderService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
@Tag(name = "ServiceOrderUserController", description = "유저의 주문내역")
public class ServiceOrderUserController {

    private final ServiceOrderService serviceOrderService;

    // 자신의 주문 내역을 조회
    @Operation(summary = "주문내역", description = "주문내역을 조회한다.")
    @GetMapping("/myPage/order/history/{memberId}")
    public String orderHist(@PathVariable Integer memberId, Model model) {
        List<ServiceOrderDTO> serviceOrders = serviceOrderService.getOrdersByMemberId(memberId);
        model.addAttribute("serviceOrders", serviceOrders);
        return "/myPage/order/history";
    }

    @ResponseBody
    @GetMapping("/api/user/orders/{memberId}")
    public ResponseEntity<List<ServiceOrderDTO>> getOrdersByMemberId(@PathVariable Integer memberId) {
        return ResponseEntity.ok(serviceOrderService.getOrdersByMemberId(memberId));
    }
}
