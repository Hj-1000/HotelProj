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




}



