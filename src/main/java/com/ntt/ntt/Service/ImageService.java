package com.ntt.ntt.Service;

import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Util.FileUpload;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ImageService {

    private final ImageRepository imageRepository;
    private final FileUpload fileUpload;
    @Value("${dataUploadPath}")
    private String IMG_LOCATION;
//    private static final String IMG_LOCATION = "c:/ntt/images/"; // 이미지 저장 경로

    // 호텔 이미지 등록
    public String registerHotelImage(Integer hotelId, MultipartFile imageFile) {
        String filename = fileUpload.FileUpload(IMG_LOCATION, imageFile);
        if (filename == null) {
            throw new RuntimeException("파일 업로드 실패");
        }
        try {
            Image image = new Image();
            image.setHotel(new Hotel(hotelId));
            image.setImageName(filename);
            image.setImageOriginalName(imageFile.getOriginalFilename());
            image.setImagePath(IMG_LOCATION + filename);
            imageRepository.save(image);
        } catch (Exception e) {
            // 데이터베이스 저장 중 예외 처리
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }


        return filename;
    }

    // 방 이미지 등록
    public String registerRoomImage(Integer roomId, MultipartFile imageFile) {
        String filename = fileUpload.FileUpload(IMG_LOCATION, imageFile);
        if (filename == null) {
            throw new RuntimeException("파일 업로드 실패");
        }
        try {
            Image image = new Image();
            image.setRoom(new Room(roomId));
            image.setImageName(filename);
            image.setImageOriginalName(imageFile.getOriginalFilename());
            image.setImagePath(IMG_LOCATION + filename);
            imageRepository.save(image);
        } catch (Exception e) {
            // 데이터베이스 저장 중 예외 처리
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }
        return filename;
    }

    // 메뉴 카테고리 이미지 등록
    public String registerServiceCateImage(Integer serviceCateId, MultipartFile imageFile) {
        // 파일 업로드
        String filename = fileUpload.FileUpload(IMG_LOCATION, imageFile);
        System.out.println(filename);
        if (filename == null) {
            throw new RuntimeException("파일 업로드 실패");
        }
        try {
            // Image 엔티티 생성 및 저장
            Image image = new Image();
            image.setServiceCate(new ServiceCate(serviceCateId)); // ServiceCate 객체 생성
            image.setImageName(filename);
            image.setImageOriginalName(imageFile.getOriginalFilename());
            image.setImagePath(IMG_LOCATION + filename); //IMG_LOCATION + filename);
            log.info("Image to be saved: {}", image);
            imageRepository.save(image); // 데이터베이스 저장

        } catch (Exception e) {
            // 데이터베이스 저장 중 예외 처리
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filename; // 저장된 파일 이름 반환
    }

    // 메뉴 이미지 등록
    public String registerServiceMenuImage(Integer serviceMenuId, MultipartFile imageFile) {
        String filename = fileUpload.FileUpload(IMG_LOCATION, imageFile);
        if (filename == null) {
            throw new RuntimeException("파일 업로드 실패");
        }
        try {
            Image image = new Image();
            image.setServiceMenu(new ServiceMenu(serviceMenuId));
            image.setImageName(filename);
            image.setImageOriginalName(imageFile.getOriginalFilename());
            image.setImagePath(IMG_LOCATION + filename);
            imageRepository.save(image);
        } catch (Exception e) {
            // 데이터베이스 저장 중 예외 처리
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filename;
    }
    // 배너 이미지 등록
    public String registerBannerImage(Integer bannerId, MultipartFile imageFile) {
        String filename = fileUpload.FileUpload(IMG_LOCATION, imageFile);
        if (filename == null) {
            throw new RuntimeException("파일 업로드 실패");
        }

        try {
            Image image = new Image();
            image.setBanner(new Banner(bannerId));
            image.setImageName(filename);
            image.setImageOriginalName(imageFile.getOriginalFilename());
            image.setImagePath(IMG_LOCATION + filename);
            imageRepository.save(image);
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filename;
    }

    // 공지사항 이미지 등록
    public String registerNoticeImage(Integer noticeId, MultipartFile imageFile) {
        String filename = fileUpload.FileUpload(IMG_LOCATION, imageFile);
        if (filename == null) {
            throw new RuntimeException("파일 업로드 실패");
        }
        try {
            Image image = new Image();
            image.setNotice(new Notice(noticeId));
            image.setImageName(filename);
            image.setImageOriginalName(imageFile.getOriginalFilename());
            image.setImagePath(IMG_LOCATION + filename);
            imageRepository.save(image);
        } catch (Exception e) {
            // 데이터베이스 저장 중 예외 처리
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filename;
    }

    // 호텔 이미지 목록
    public List<Image> listHotelImages(Integer hotelId) {
        return imageRepository.findByHotel_HotelId(hotelId);
    }

    // 방 이미지 목록
    public List<Image> listRoomImages(Integer roomId) {
        return imageRepository.findByRoom_RoomId(roomId);
    }

    // 메뉴 카테고리 이미지 목록
    public List<Image> listServiceCateImages(Integer serviceCateId) {
        return imageRepository.findByServiceCate_ServiceCateId(serviceCateId);
    }

    // 메뉴 이미지 목록
    public List<Image> listServiceMenuImages(Integer serviceMenuId) {
        return imageRepository.findByServiceMenu_ServiceMenuId(serviceMenuId);
    }

    // 배너 이미지 목록
    public List<Image> listBannerImages(Integer bannerId) {
        return imageRepository.findByBanner_BannerId(bannerId);
    }

    // 공지사항 이미지 목록
    public List<Image> listNoticeImages(Integer noticeId) {
        return imageRepository.findByNotice_NoticeId(noticeId);
    }

    // 이미지 상세보기
    public Image readImageDetails(Integer imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));
    }

    // 이미지 수정
    public String updateImage(Integer imageId, MultipartFile newImageFile) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

        // 기존 파일 삭제
        fileUpload.FileDelete(IMG_LOCATION, image.getImageName());

        // 새 파일 업로드
        String newFilename = fileUpload.FileUpload(IMG_LOCATION, newImageFile);
        if (newFilename == null) {
            throw new RuntimeException("파일 업로드 실패");
        }

        // 이미지 정보 업데이트
        image.setImageName(newFilename);
        image.setImageOriginalName(newImageFile.getOriginalFilename());
        image.setImagePath(IMG_LOCATION + newFilename);
        imageRepository.save(image);

        return newFilename;
    }

    // 이미지 삭제
    public void deleteImage(Integer imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

        // 파일 삭제
        fileUpload.FileDelete(IMG_LOCATION, image.getImageName());

        // 데이터베이스에서 삭제
        imageRepository.delete(image);
    }
}
