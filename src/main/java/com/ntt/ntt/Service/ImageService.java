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

    @Value("c:/data/")
    private String IMG_LOCATION;

    // 본사 이미지 등록
    public List<String> registerCompanyImage(Integer companyId, List<MultipartFile> imageFiles) {
        // 파일 업로드 처리
        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);
        if (filenames == null || filenames.contains(null)) {
            throw new RuntimeException("파일 업로드 실패");
        }

        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                if (filename != null) {
                    Image image = new Image();
                    image.setCompany(new Company(companyId));
                    image.setImageName(filename);
                    image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
                    image.setImagePath(IMG_LOCATION + filename);
                    imageRepository.save(image); // DB 저장
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filenames; // 저장된 파일명 리스트 반환
    }

    // 호텔 이미지 등록
    public List<String> registerHotelImage(Integer hotelId, List<MultipartFile> imageFiles) {
        // 파일 업로드 처리
        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);
        if (filenames == null || filenames.contains(null)) {
            throw new RuntimeException("파일 업로드 실패");
        }

        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                if (filename != null) {
                    Image image = new Image();
                    image.setHotel(new Hotel(hotelId));
                    image.setImageName(filename);
                    image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
                    image.setImagePath(IMG_LOCATION + filename);
                    imageRepository.save(image); // DB 저장
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filenames; // 저장된 파일명 리스트 반환
    }

    // 방 이미지 등록
    public List<String> registerRoomImage(Integer roomId, List<MultipartFile> imageFiles) {
        // 파일 업로드 처리
        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);
        if (filenames == null || filenames.contains(null)) {
            throw new RuntimeException("파일 업로드 실패");
        }

        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                if (filename != null) {
                    Image image = new Image();
                    image.setRoom(new Room(roomId)); // 수정: roomId 사용
                    image.setImageName(filename);
                    image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
                    image.setImagePath(IMG_LOCATION + filename);
                    imageRepository.save(image); // DB 저장
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filenames; // 저장된 파일명 리스트 반환
    }

    // 메뉴 카테고리 이미지 등록
    public List<String> registerServiceCateImage(Integer serviceCateId, List<MultipartFile> imageFiles) {
        // 파일 업로드 처리
        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);
        if (filenames == null || filenames.contains(null)) {
            throw new RuntimeException("파일 업로드 실패");
        }

        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                if (filename != null) {
                    Image image = new Image();
                    image.setServiceCate(new ServiceCate(serviceCateId)); // 수정: serviceCateId 사용
                    image.setImageName(filename);
                    image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
                    image.setImagePath(IMG_LOCATION + filename);
                    imageRepository.save(image); // DB 저장
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filenames; // 저장된 파일명 리스트 반환
    }

    // 메뉴 이미지 등록
    public List<String> registerServiceMenuImage(Integer serviceMenuId, List<MultipartFile> imageFiles) {
        // 파일 업로드 처리
        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);
        if (filenames == null || filenames.contains(null)) {
            throw new RuntimeException("파일 업로드 실패");
        }

        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                if (filename != null) {
                    Image image = new Image();
                    image.setServiceMenu(new ServiceMenu(serviceMenuId)); // 수정: serviceMenuId 사용
                    image.setImageName(filename);
                    image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
                    image.setImagePath(IMG_LOCATION + filename);
                    imageRepository.save(image); // DB 저장
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filenames; // 저장된 파일명 리스트 반환
    }

    // 배너 이미지 등록
    public List<String> registerBannerImage(Integer bannerId, List<MultipartFile> imageFiles) {
        // 파일 업로드 처리
        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);
        if (filenames == null || filenames.contains(null)) {
            throw new RuntimeException("파일 업로드 실패");
        }

        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                if (filename != null) {
                    Image image = new Image();
                    image.setBanner(new Banner(bannerId)); // 수정: bannerId 사용
                    image.setImageName(filename);
                    image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
                    image.setImagePath(IMG_LOCATION + filename);
                    imageRepository.save(image); // DB 저장
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filenames; // 저장된 파일명 리스트 반환
    }

    // 공지사항 이미지 등록
    public List<String> registerNoticeImage(Integer noticeId, List<MultipartFile> imageFiles) {
        // 파일 업로드 처리
        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);
        if (filenames == null || filenames.contains(null)) {
            throw new RuntimeException("파일 업로드 실패");
        }

        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                if (filename != null) {
                    Image image = new Image();
                    image.setNotice(new Notice(noticeId)); // 수정: noticeId 사용
                    image.setImageName(filename);
                    image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
                    image.setImagePath(IMG_LOCATION + filename);
                    imageRepository.save(image); // DB 저장
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        return filenames; // 저장된 파일명 리스트 반환
    }


    // 이미지 수정
    public String updateImage(Integer imageId, List<MultipartFile> newImageFile) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

        // 기존 파일 삭제
        fileUpload.FileDelete(IMG_LOCATION, image.getImageName());

        // 새 파일 업로드
        List<String> newFilenames = fileUpload.FileUpload(IMG_LOCATION, newImageFile);
        if (newFilenames == null || newFilenames.isEmpty()) {
            throw new RuntimeException("파일 업로드 실패");
        }

        // 이미지 정보 업데이트
        String newFilename = newFilenames.get(0); // 여러 파일이 업로드될 수 있지만, 첫 번째 파일만 업데이트
        image.setImageName(newFilename);
        image.setImageOriginalName(newImageFile.get(0).getOriginalFilename());
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