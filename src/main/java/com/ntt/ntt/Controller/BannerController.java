package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.BannerDTO;
import com.ntt.ntt.Entity.Banner;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ntt.ntt.Service.ImageService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log

//@RequestMapping("/banner")
public class BannerController {


    private final BannerService bannerService;

    // 배너 목록 페이지
    @GetMapping("/banner/list")
    public String listBanners(Model model) {
//        List<Banner> banners = bannerService.getAllBanners();
//        model.addAttribute("banners", banners);
        return "banner/list";
    }

    @GetMapping("/banner/create")
    public String createForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if(userDetails == null) {
            return "redirect:/login";
        }
        model.addAttribute("banner", new Banner());
        return "banner/create";
    }

    @PostMapping("/banner/create")
    public String createProc(BannerDTO bannerDTO, @RequestParam("multipartFile") List<MultipartFile> multipartFiles,
                             RedirectAttributes redirectAttributes) {
        bannerService.register(bannerDTO, multipartFiles);
        redirectAttributes.addFlashAttribute("banner", bannerDTO);
        return "redirect:/banner/list";
    }

    // 배너 상세 페이지
    @GetMapping("/banner/detail")
    public String detail(@RequestParam Integer bannerId, Model model) {
        BannerDTO bannerDTO = bannerService.read(bannerId);
        model.addAttribute("banner", bannerDTO);
        return "banner/detail";

    }





    // 배너 삭제
    @GetMapping("/banner/delete")
    public String deleteForm(@RequestParam Integer bannerId, List<MultipartFile> MultipartFile){
        bannerService.delete(bannerId);

        return "redirect:/banner/list"; // 삭제 후 목록 페이지로 리다이렉트
    }
}
