package com.ntt.ntt.Controller;

import com.ntt.ntt.Entity.Banner;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
//@RequestMapping("/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    // 배너 목록 페이지
    @GetMapping("/banner/list")
    public String listBanners(Model model) {
        List<Banner> banners = bannerService.getAllBanners();
        model.addAttribute("banners", banners);
        return "banner/list"; // list.html 뷰 페이지
    }

    // 배너 상세 페이지
    @GetMapping("/banner/{bannerId}")
    public String getBanner(@PathVariable Integer bannerId, Model model) {
        Optional<Banner> banner = bannerService.getBannerById(bannerId);
        if (banner.isPresent()) {
            model.addAttribute("banner", banner.get());
            return "banner/detail"; // detail.html 뷰 페이지
        } else {
            return "redirect:/banner/list"; // 배너가 없으면 목록으로 리다이렉트
        }
    }

    // 배너 생성 페이지
    @GetMapping("/banner/create")
    public String createBannerForm() {
        return "banner/create"; // create.html 뷰 페이지
    }

    // 배너 생성 처리
    @PostMapping("/banner/create")
    public String createBanner(Banner banner) {
        bannerService.createBanner(banner);
        return "redirect:/banner/list"; // 생성 후 목록 페이지로 리다이렉트
    }

    // 배너에 이미지 추가
    @PostMapping("/banner/{bannerId}/addImage")
    public String registerBannerImage(@PathVariable Integer bannerId,
                                   @RequestParam("imageFile") MultipartFile imageFile,
                                   Model model) throws IOException {

        Image image = new Image();
        image.setImageName(imageFile.getOriginalFilename());
        image.setImagePath("/uploads/" + imageFile.getOriginalFilename()); // 예시 경로
        image.setImageMain("false"); // 기본적으로 대표이미지가 아닌 경우

        // 이미지 파일 저장 로직 추가 (예: 서버에 저장)
        // imageFile.transferTo(new File("경로"));

        bannerService.registerBannerImage(bannerId, image);
        return "redirect:/banner/{bannerId}"; // 이미지 추가 후 상세 페이지로 리다이렉트
    }

    // 배너 상태 변경
    @PostMapping("/banner/updateStatus/{bannerId}")
    public String updateStatus(@PathVariable Integer bannerId, @RequestParam Boolean status) {
        bannerService.updateBannerStatus(bannerId, status);
        return "redirect:/banner/list"; // 상태 변경 후 목록 페이지로 리다이렉트
    }

    // 배너 순위 변경
    @PostMapping("/banner/updateRank/{bannerId}")
    public String updateRank(@PathVariable Integer bannerId, @RequestParam Integer rank) {
        bannerService.updateBannerRank(bannerId, rank);
        return "redirect:/banner/list"; // 순위 변경 후 목록 페이지로 리다이렉트
    }

    // 배너 삭제
    @PostMapping("/banner/delete/{bannerId}")
    public String deleteBanner(@PathVariable Integer bannerId) {
        bannerService.deleteBanner(bannerId);
        return "redirect:/banner/list"; // 삭제 후 목록 페이지로 리다이렉트
    }
}
