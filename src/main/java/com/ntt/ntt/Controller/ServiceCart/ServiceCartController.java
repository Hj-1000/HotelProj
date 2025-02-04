package com.ntt.ntt.Controller.ServiceCart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager/cart")
public class ServiceCartController {

    @GetMapping
    public String getCartPage(Model model) {
        // 로그인한 회원의 ID를 세션 또는 인증 정보에서 가져오도록 설정할 수 있습니다.
        Integer memberId = 1;  // 로그인한 회원의 ID, 실제로는 세션에서 가져옵니다.

        // 추가적으로 모델에 데이터를 추가할 수 있습니다.
        model.addAttribute("memberId", memberId);

        // cart.html 템플릿을 반환
        return "manager/roomservice/cart/cart";
    }
}
