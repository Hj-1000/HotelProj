package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.BannerDTO;
import com.ntt.ntt.Service.BannerService;
import jakarta.mail.Multipart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
//@RequestMapping("/admin/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    // 배너 목록 조회 (웹 페이지)
    @GetMapping("/banner/list")
    public String getAllBanners(Model model) {
        List<BannerDTO> banners = bannerService.getAllBanners();
        model.addAttribute("banners", banners);
        return "/banner/list"; // 'banner/list.html' 뷰로 이동
    }

    // 배너 등록 폼 (웹 페이지)
    @GetMapping("/banner/register")
    public String showCreateBannerForm(Model model) {
        model.addAttribute("bannerDTO", new BannerDTO());
        return "/banner/register"; // 'banner/create.html' 뷰로 이동
    }

    // 배너 등록 처리 (웹 페이지)
    @PostMapping("/banner/register")
    public String registerBanner(@ModelAttribute BannerDTO bannerDTO, @RequestParam("multipartFile") List<MultipartFile> multipartFile) {
        bannerService.registerBanner(bannerDTO,multipartFile);
        return "redirect:/banner/list"; // 배너 목록 페이지로 리디렉션
    }

    // 배너 수정 폼 (웹 페이지)
    @GetMapping("/update/{bannerId}")
    public String showEditBannerForm(@PathVariable Integer bannerId, Model model) {
        BannerDTO bannerDTO = bannerService.getBannerById(bannerId);
        model.addAttribute("bannerDTO", bannerDTO);
        return "banner/update"; // 'banner/edit.html' 뷰로 이동
    }

    // 배너 수정 처리 (웹 페이지)
    @PostMapping("/update/{bannerId}")
    public String updateBanner(@PathVariable Integer bannerId, @ModelAttribute BannerDTO bannerDTO) {
        bannerService.updateBanner(bannerId, bannerDTO);
        return "redirect:/banner/list"; // 배너 목록 페이지로 리디렉션
    }

    // 배너 삭제 (웹 페이지)
    @GetMapping("/delete/{bannerId}")
    public String deleteBanner(@PathVariable Integer bannerId) {
        bannerService.deleteBanner(bannerId);
        return "redirect:/banner/list"; // 배너 목록 페이지로 리디렉션
    }

    // 배너 목록 조회 (API)
    @GetMapping("/api")
    @ResponseBody
    public List<BannerDTO> getBannersApi() {
        return bannerService.getAllBanners();
    }

    // 배너 등록 (API)
    @PostMapping("/api")
    @ResponseBody
    public BannerDTO registerBannerApi(@RequestBody BannerDTO bannerDTO, @RequestParam("multipartFile") List<MultipartFile> multipartFile) {
        return bannerService.registerBanner(bannerDTO,multipartFile);
    }

    // 배너 수정 (API)
    @PutMapping("/api/{bannerId}")
    @ResponseBody
    public BannerDTO updateBannerApi(@PathVariable Integer bannerId, @RequestBody BannerDTO bannerDTO) {
        return bannerService.updateBanner(bannerId, bannerDTO);
    }

    // 배너 삭제 (API)
    @DeleteMapping("/api/{bannerId}")
    @ResponseBody
    public void deleteBannerApi(@PathVariable Integer bannerId) {
        bannerService.deleteBanner(bannerId);
    }
}
