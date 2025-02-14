package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.BannerDTO;
import com.ntt.ntt.Entity.Banner;
import com.ntt.ntt.Repository.BannerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class BannerService {

    private final BannerRepository bannerRepository;
    private final ModelMapper modelMapper;
    private final ImageService imageService;

    // 배너 등록
    public BannerDTO registerBanner(BannerDTO bannerDTO, List<MultipartFile> multipartFile) {
        Banner banner =
//                Banner.builder()
//                .bannerTitle(bannerDTO.getBannerTitle())
//                .bannerStatus(bannerDTO.getBannerStatus())
//                .bannerRank(bannerDTO.getBannerRank())
//                .company(bannerDTO.getCompanyId())
//                .build();
                modelMapper.map(bannerDTO, Banner.class);

        Banner savedBanner = bannerRepository.save(banner);
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageService.registerBannerImage(banner.getBannerId(), multipartFile);
        }
        return convertToDTO(savedBanner);
    }

    // 배너 목록 조회
    public List<BannerDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 배너 단건 조회
    public BannerDTO getBannerById(Integer bannerId) {
        return bannerRepository.findById(bannerId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다."));
    }

    // 배너 수정
    public BannerDTO updateBanner(Integer bannerId, BannerDTO bannerDTO) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다."));

        banner.setBannerTitle(bannerDTO.getBannerTitle());
        banner.setBannerStatus(bannerDTO.getBannerStatus());
        banner.setBannerRank(bannerDTO.getBannerRank());
        banner.setCompany(bannerDTO.getCompanyId());

        Banner updatedBanner = bannerRepository.save(banner);
        return convertToDTO(updatedBanner);
    }

    // 배너 삭제
    public void deleteBanner(Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다."));
        bannerRepository.delete(banner);
    }

    // Entity -> DTO 변환 메서드
    private BannerDTO convertToDTO(Banner banner) {
        return new BannerDTO(
                banner.getBannerId(),
                banner.getBannerTitle(),
                banner.getBannerStatus(),
                banner.getBannerRank(),
                banner.getCompany(),
                banner.getRegDate(),
                banner.getModDate()
        );
    }
}
