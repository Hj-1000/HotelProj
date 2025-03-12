package com.ntt.ntt.Controller.serviceOrder;

import com.ntt.ntt.DTO.ServiceOrderHistoryDTO;
import com.ntt.ntt.DTO.ServiceOrderUpdateDTO;
import com.ntt.ntt.Service.ServiceOrderService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("admin/order") //url roomService아래에
public class ServiceOrderAdminController {
    private final ServiceOrderService serviceOrderService;
    private final PaginationUtil paginationUtil;

    @GetMapping("/list")
    public String orderListSearch(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String searchType,
                             @PageableDefault(page = 1) Pageable page, Model model) {


        Page<ServiceOrderHistoryDTO> serviceOrderHistoryDTOS =
                serviceOrderService.adminOrderList(page, keyword,searchType);

        Map<String, Integer> pageInfo = paginationUtil.pagination(serviceOrderHistoryDTOS);

        // 전체 페이지 수
        int totalPages = serviceOrderHistoryDTOS.getTotalPages();
        // 현재 페이지 수
        int currentPage = pageInfo.get("currentPage");

        // 시작 페이지와 끝 페이지 계산 (현재 페이지를 기준으로 최대 10페이지까지)
        int startPage = Math.max(1, currentPage - 4); // 10개씩 끊어서 시작 페이지 계산
        int endPage = Math.min(startPage + 9, totalPages); // 최대 페이지 수를 넘기지 않도록

        // 페이지 정보 업데이트 (동적으로 계산된 startPage, endPage)
        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);


        model.addAttribute("serviceOrderHistoryDTOS", serviceOrderHistoryDTOS);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);


        return "/manager/roomService/order/list";
    }

    @GetMapping("/read")
    public String orderRead(@RequestParam("serviceOrderId") Integer serviceOrderId, Model model) {
        // 주문 정보 가져오기
        ServiceOrderHistoryDTO serviceOrderHistoryDTO = serviceOrderService.getOrderDetail(serviceOrderId);
        model.addAttribute("serviceOrderHistoryDTO", serviceOrderHistoryDTO);
        return "/manager/roomService/order/read";
    }

    // 주문 수정 페이지
    @GetMapping("/update")
    public String orderEdit(@RequestParam("serviceOrderId") Integer serviceOrderId, Model model) {
        ServiceOrderHistoryDTO serviceOrderHistoryDTO = serviceOrderService.getOrderDetail(serviceOrderId);
        model.addAttribute("serviceOrderHistoryDTO", serviceOrderHistoryDTO);
        return "/manager/roomService/order/update";
    }

    // 주문 수정 처리
    @PostMapping("/update")
    public String orderUpdate(@ModelAttribute ServiceOrderUpdateDTO updateDTO, RedirectAttributes redirectAttributes) {
        serviceOrderService.updateOrder(updateDTO);
        redirectAttributes.addFlashAttribute("message", "주문이 성공적으로 수정되었습니다.");
        return "redirect:/admin/order/list";
    }

    // 주문 삭제
    @GetMapping("/delete")
    public String orderDelete(@RequestParam("serviceOrderId") Integer serviceOrderId, RedirectAttributes redirectAttributes) {
        serviceOrderService.deleteOrder(serviceOrderId);
        redirectAttributes.addFlashAttribute("message", "주문이 삭제되었습니다.");
        return "redirect:/admin/order/list";
    }

}
