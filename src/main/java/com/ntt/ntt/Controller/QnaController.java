package com.ntt.ntt.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class QnaController {
    // getmapping 으로 페이지 접속 잘되는지 확인
    @GetMapping("/qna")
    public String qnaForm() {
     return  null;
 }
}
