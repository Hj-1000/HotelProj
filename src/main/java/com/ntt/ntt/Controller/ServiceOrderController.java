package com.ntt.ntt.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager/order")
public class ServiceOrderController {
    @GetMapping
    public String getOrderPage(Model model) {
        return "manager/roomservice/order/order"; // order.html 페이지 반환
    }
}
