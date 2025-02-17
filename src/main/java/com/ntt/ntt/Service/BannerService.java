package com.ntt.ntt.Service;

import com.ntt.ntt.Entity.Banner;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ntt.ntt.Repository.ImageRepository;
import java.util.List;
import java.util.Optional;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;
    @Autowired
    private ImageRepository imageRepository;

    // 배너 생성
    public Banner createBanner(Banner banner) {
        return bannerRepository.save(banner);
    }

    // 배너에 이미지 추가
    public Image registerBannerImage(Integer bannerId, Image image) {
        Optional<Banner> bannerOptional = bannerRepository.findById(bannerId);
        if (bannerOptional.isPresent()) {
            Banner banner = bannerOptional.get();
            image.setBanner(banner); // 이미지에 배너 설정
            return imageRepository.save(image);
        }
        return null;
    }

    // 배너 조회 (배너 ID로)
    public Optional<Banner> getBannerById(Integer bannerId) {
        return bannerRepository.findById(bannerId);
    }

    // 모든 배너 조회
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    // 배너 삭제
    public void deleteBanner(Integer bannerId) {
        bannerRepository.deleteById(bannerId);
    }

    // 배너 상태 변경 (예: 활성화/비활성화)
    public Banner updateBannerStatus(Integer bannerId, Boolean status) {
        Optional<Banner> bannerOptional = bannerRepository.findById(bannerId);
        if (bannerOptional.isPresent()) {
            Banner banner = bannerOptional.get();
            banner.setBannerStatus(status);
            return bannerRepository.save(banner);
        }
        return null;
    }

    // 배너 순위 변경
    public Banner updateBannerRank(Integer bannerId, Integer rank) {
        Optional<Banner> bannerOptional = bannerRepository.findById(bannerId);
        if (bannerOptional.isPresent()) {
            Banner banner = bannerOptional.get();
            banner.setBannerRank(rank);
            return bannerRepository.save(banner);
        }
        return null;
    }


}





//package com.ntt.ntt.Service;
//
//import com.ntt.ntt.Entity.Banner;
//import com.ntt.ntt.Entity.Image;
//import com.ntt.ntt.Repository.ImageRepository;
//import com.ntt.ntt.Util.FileUpload;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//@Log4j2
//public class BannerService {
//
//    private final ImageRepository imageRepository;
//    private final FileUpload fileUpload;
//
//    @Value("${dataUploadPath}")
//    private String IMG_LOCATION;
//
//    // 배너 이미지 등록
//    public List<String> registerBannerImage(Integer bannerId, List<MultipartFile> imageFiles) {
//        // 파일 업로드 처리
//        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);
//        if (filenames == null || filenames.contains(null)) {
//            throw new RuntimeException("파일 업로드 실패");
//        }
//
//        try {
//            for (int i = 0; i < filenames.size(); i++) {
//                String filename = filenames.get(i);
//                if (filename != null) {
//                    Image image = new Image();
//                    image.setBanner(new Banner(bannerId)); // 배너 ID 사용
//                    image.setImageName(filename);
//                    image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
//                    image.setImagePath(IMG_LOCATION + filename);
//                    imageRepository.save(image); // DB에 배너 이미지 저장
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
//        }
//
//        return filenames; // 저장된 파일명 리스트 반환
//    }
//
//    // 배너 이미지 수정
//    public String updateBannerImage(Integer imageId, List<MultipartFile> newImageFile) {
//        Image image = imageRepository.findById(imageId)
//                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));
//
//        // 기존 파일 삭제
//        fileUpload.FileDelete(IMG_LOCATION, image.getImageName());
//
//        // 새 파일 업로드
//        List<String> newFilenames = fileUpload.FileUpload(IMG_LOCATION, newImageFile);
//        if (newFilenames == null || newFilenames.isEmpty()) {
//            throw new RuntimeException("파일 업로드 실패");
//        }
//
//        // 이미지 정보 업데이트
//        String newFilename = newFilenames.get(0); // 여러 파일이 업로드될 수 있지만, 첫 번째 파일만 업데이트
//        image.setImageName(newFilename);
//        image.setImageOriginalName(newImageFile.get(0).getOriginalFilename());
//        image.setImagePath(IMG_LOCATION + newFilename);
//        imageRepository.save(image);
//
//        return newFilename;
//    }
//
//    // 배너 이미지 삭제
//    public void deleteBannerImage(Integer imageId) {
//        Image image = imageRepository.findById(imageId)
//                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));
//
//        // 파일 삭제
//        fileUpload.FileDelete(IMG_LOCATION, image.getImageName());
//
//        // 데이터베이스에서 삭제
//        imageRepository.delete(image);
//    }
//    // 배너 이미지 목록을 가져오는 메서드
//    public List<String> getAllBannerImages() {
//        // 예시: static 폴더 내 이미지 목록을 반환하거나 DB에서 데이터를 조회
//        return List.of("banner1.jpg", "banner2.jpg", "banner3.jpg");
//    }
//}







//package com.ntt.ntt.Service;
//
//import com.ntt.ntt.DTO.BannerDTO;
//import com.ntt.ntt.Entity.Banner;
//import com.ntt.ntt.Repository.BannerRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//@Log4j2
//public class BannerService {
//
//    private final BannerRepository bannerRepository;
//    private final ModelMapper modelMapper;
//    private final ImageService imageService;
//
//    // 배너 등록
//    public BannerDTO registerBanner(BannerDTO bannerDTO, List<MultipartFile> multipartFile) {
//        Banner banner =
////                Banner.builder()
////                .bannerTitle(bannerDTO.getBannerTitle())
////                .bannerStatus(bannerDTO.getBannerStatus())
////                .bannerRank(bannerDTO.getBannerRank())
////                .company(bannerDTO.getCompanyId())
////                .build();
//                modelMapper.map(bannerDTO, Banner.class);
//
//        Banner savedBanner = bannerRepository.save(banner);
//        if (multipartFile != null && !multipartFile.isEmpty()) {
//            imageService.registerBannerImage(banner.getBannerId(), multipartFile);
//        }
//        return convertToDTO(savedBanner);
//    }
//
//    // 배너 목록 조회
//    public List<BannerDTO> getAllBanners() {
//        return bannerRepository.findAll().stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    // 배너 단건 조회
//    public BannerDTO getBannerById(Integer bannerId) {
//        return bannerRepository.findById(bannerId)
//                .map(this::convertToDTO)
//                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다."));
//    }
//
//    // 배너 수정
//    public BannerDTO updateBanner(Integer bannerId, BannerDTO bannerDTO) {
//        Banner banner = bannerRepository.findById(bannerId)
//                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다."));
//
//        banner.setBannerTitle(bannerDTO.getBannerTitle());
//        banner.setBannerStatus(bannerDTO.getBannerStatus());
//        banner.setBannerRank(bannerDTO.getBannerRank());
//        banner.setCompany(bannerDTO.getCompanyId());
//
//        Banner updatedBanner = bannerRepository.save(banner);
//        return convertToDTO(updatedBanner);
//    }
//
//    // 배너 삭제
//    public void deleteBanner(Integer bannerId) {
//        Banner banner = bannerRepository.findById(bannerId)
//                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다."));
//        bannerRepository.delete(banner);
//    }
//
//    // Entity -> DTO 변환 메서드
//    private BannerDTO convertToDTO(Banner banner) {
//        return new BannerDTO(
//                banner.getBannerId(),
//                banner.getBannerTitle(),
//                banner.getBannerStatus(),
//                banner.getBannerRank(),
//                banner.getCompany(),
//                banner.getRegDate(),
//                banner.getModDate()
//        );
//    }
//}
