package com.ntt.ntt.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StartController {

    @GetMapping("/admin/executive")
    public String a(){
        return "admin/executive";
    }

    @GetMapping("/admin/executiveRegister")
    public String b(){
        return "admin/executiveRegister";
    }

    @GetMapping("/admin/hotelHeadquarters")
    public String c(){
        return "admin/hotelHeadquarters";
    }

    @GetMapping("/admin/hotelHeadquartersRegister")
    public String d(){
        return "admin/hotelHeadquartersRegister";
    }

    @GetMapping("/roomList")
    public String e(){
        return "roomList";
    }
}
