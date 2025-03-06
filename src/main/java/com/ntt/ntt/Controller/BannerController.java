package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.BannerDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.Entity.Banner;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Repository.BannerRepository;
import com.ntt.ntt.Repository.ImageRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ntt.ntt.Service.ImageService;
import java.util.List;
import java.util.stream.Collectors;

@Controller
//@RequiredArgsConstructor
@Log

//@RequestMapping("/banner")
public class BannerController {


    private final BannerService bannerService;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    @Autowired
    public BannerController(BannerService bannerService, ImageService imageService, ImageRepository imageRepository) {
        this.bannerService = bannerService;
        this.imageService = imageService;
        this.imageRepository=imageRepository;

    }

    // 배너 목록 페이지
//    @GetMapping("/banner/list")
//    public String listBanners(Model model) {
//        List<BannerDTO> bannerDTOList = bannerService.list();
//        model.addAttribute("bannerDTOList", bannerDTOList);
//
//        return "banner/list";
//    }
    @GetMapping("/banner/list")
    public String bannerList(Model model) {
        List<BannerDTO> bannerDTOList = bannerService.list(); // 배너 목록 가져오기

        for (BannerDTO banner : bannerDTOList) {
            List<Image> images = imageRepository.findByBanner_BannerId(banner.getBannerId());

            List<ImageDTO> imageDTOs = images.stream()
                    .map(image -> {
                        ImageDTO dto = ImageDTO.fromEntity(image);
                        // 이미지 경로가 전체 경로를 포함하고 있다면 파일명만 추출
                        if (dto.getImagePath() != null) {
                            if (dto.getImagePath().contains("/")) {
                                dto.setImagePath(dto.getImagePath().substring(dto.getImagePath().lastIndexOf("/") + 1));
                            }
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());

            banner.setBannerImageDTOList(imageDTOs);
        }

        model.addAttribute("bannerDTOList", bannerDTOList);

//        List<Banner> banners=bannerService.list();
//        model.addAttribute("banners", banners);
        return "banner/list";
    }


    @GetMapping("/banner/register")
    public String registerForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        model.addAttribute("banner", new Banner());
        return "/banner/register";
    }

    @PostMapping("/banner/register")
    public String registerProc(BannerDTO bannerDTO, @RequestParam("multipartFile") List<MultipartFile> multipartFiles,
                               RedirectAttributes redirectAttributes) {
        bannerService.register(bannerDTO, multipartFiles);
        redirectAttributes.addFlashAttribute("banner", "새로운 배너가 등록되었습니다");
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
//    @GetMapping("/banner/delete")
//    public String deleteForm(@RequestParam Integer bannerId, List<MultipartFile> MultipartFile){
//        bannerService.delete(bannerId);
//
//        return "redirect:/banner/list"; // 삭제 후 목록 페이지로 리다이렉트
//    }

    @PostMapping("/banner/delete")
    public String deleteBanner(@RequestParam Integer bannerId) {
        bannerService.delete(bannerId);
        return "redirect:/banner/list";
    }

}
