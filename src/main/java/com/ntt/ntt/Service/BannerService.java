package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.BannerDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.NoticeDTO;
import com.ntt.ntt.Entity.Banner;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Entity.Notice;
import com.ntt.ntt.Repository.BannerRepository;
import com.ntt.ntt.Repository.NoticeRepository;
import com.ntt.ntt.Util.FileUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ntt.ntt.Repository.ImageRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2

public class BannerService {

    private final BannerRepository bannerRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final FileUpload fileUpload;
    private final ModelMapper modelMapper;

    @Value("c:/data/")
    private String IMG_LOCATION;

    public void register(BannerDTO bannerDTO, List<MultipartFile> multipartFile) {
        Banner banner = modelMapper.map(bannerDTO, Banner.class);
        bannerRepository.save(banner);
        if(multipartFile != null && !multipartFile.isEmpty()) {
            imageService.registerBannerImage(banner.getBannerId(), multipartFile);}
    }

    public void update(BannerDTO bannerDTO, List<MultipartFile> multipartFile) {
        Optional<Banner> banner = bannerRepository.findById(bannerDTO.getBannerId());
        if(banner.isPresent()) {
            Banner bannerEntity = modelMapper.map(bannerDTO, Banner.class);
            bannerRepository.save(bannerEntity);
        }else{
            Banner bannerEntity = modelMapper.map(bannerDTO, Banner.class);
            bannerRepository.save(bannerEntity);
        }
    }

    public List<BannerDTO> list() {
        List<Banner> banners = bannerRepository.findAll();
        List<BannerDTO> bannerDTOList = Arrays.asList(modelMapper.map(banners, BannerDTO[].class));
        return bannerDTOList;
    }

    public BannerDTO read(Integer bannerId) {
        Optional<Banner> banner = bannerRepository.findById(bannerId);
        BannerDTO bannerDTO = modelMapper.map(banner, BannerDTO.class);
        List<ImageDTO> imageDTOList=imageRepository.findByBanner_BannerId(bannerDTO.getBannerId())
                .stream().map(imagefile->{
                    imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/",""));
                    return modelMapper.map(imagefile, ImageDTO.class);
                })
                .collect(Collectors.toList());

        bannerDTO.setBannerImageDTOList(imageDTOList);
        return bannerDTO;

    }

    @Transactional
    public void delete(Integer bannerId) {
        // 배너에 연결된 이미지 정보 조회
        List<Image> images = imageRepository.findByBanner_BannerId(bannerId);

        // 실제 이미지 파일 삭제
        for (Image image : images) {
            String imagePath = IMG_LOCATION + image.getImagePath();
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(imagePath));
            } catch (Exception e) {
                log.error("이미지 파일 삭제 실패: " + imagePath, e);
            }
        }

        // DB에서 배너 삭제 (연결된 이미지도 cascade로 자동 삭제)

        bannerRepository.deleteById(bannerId);
    }

    public List<BannerDTO> getAllBanners(String bannerTitle, String regDate, String modDate) {

        List<Banner> banners = bannerRepository.findAll();

        if (bannerTitle != null && !bannerTitle.isEmpty()) {
            banners = banners.stream()
                    .filter(banner -> banner.getBannerTitle().contains(bannerTitle))
                    .collect(Collectors.toList());
        }

        return banners.stream()
                .map(banner -> modelMapper.map(banner, BannerDTO.class))
                .collect(Collectors.toList());
    }
    //배너이미지 불러오기
    public List<Image> getBannerImages(Integer bannerId) {
        return imageRepository.findByBanner_BannerId(bannerId);
    }

    // 활성화된 배너 목록 가져오기
    public List<BannerDTO> getActiveBanners() {
        List<Banner> banners = bannerRepository.findByBannerStatus(true);
        List<BannerDTO> bannerDTOList = banners.stream()
                .map(banner -> {
                    BannerDTO dto = modelMapper.map(banner, BannerDTO.class);
                    List<Image> images = imageRepository.findByBanner_BannerId(banner.getBannerId());
                    List<ImageDTO> imageDTOs = images.stream()
                            .map(image -> {
                                ImageDTO imageDTO = ImageDTO.fromEntity(image);
                                if (imageDTO.getImagePath() != null) {
                                    if (imageDTO.getImagePath().contains("/")) {
                                        imageDTO.setImagePath(imageDTO.getImagePath().substring(imageDTO.getImagePath().lastIndexOf("/") + 1));
                                    }
                                }
                                return imageDTO;
                            })
                            .collect(Collectors.toList());
                    dto.setBannerImageDTOList(imageDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
        return bannerDTOList;
    }


}



