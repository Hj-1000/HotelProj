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
import java.util.stream.Collectors;

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

    // 객실 이미지 등록
    public List<String> registerRoomImage(Integer roomId, List<MultipartFile> imageFiles, List<String> imageTitles, List<String> imageDescriptions) {
        log.info(" [registerRoomImage] 호출됨 - Room ID: {}", roomId);
        log.info(" 업로드할 이미지 개수: {}", (imageFiles != null ? imageFiles.size() : 0));

        // 파일 업로드 처리
        List<String> filenames = fileUpload.FileUpload(IMG_LOCATION, imageFiles);

        if (filenames == null || filenames.isEmpty()) {
            throw new RuntimeException("파일 업로드 실패 - filenames가 null 또는 비어 있음");
        }

        // filenames 리스트에서 null 값 제거
        filenames = filenames.stream()
                .filter(filename -> filename != null && !filename.isEmpty())
                .collect(Collectors.toList());

        if (filenames.isEmpty()) {
            throw new RuntimeException("파일 업로드 실패 - 모든 파일이 유효하지 않음");
        }

        try {
            for (int i = 0; i < filenames.size(); i++) {
                String fullFilePath = filenames.get(i);

                if (fullFilePath == null || fullFilePath.isEmpty()) {
                    log.warn("이미지 경로가 존재하지 않음 - 원본 파일명: {}", imageFiles.get(i).getOriginalFilename());
                    continue;
                }

                // "c:/data/" 부분을 제거하고 파일명(UUID).jpg만 저장
                String savedFileName = fullFilePath.substring(fullFilePath.lastIndexOf("/") + 1);

                // **불필요한 null 데이터 방지**
                if (savedFileName == null || savedFileName.isEmpty()) {
                    log.warn("이미지 경로가 없어 저장되지 않음. 원본 파일명: {}", imageFiles.get(i).getOriginalFilename());
                    continue; // 저장하지 않고 다음 이미지 처리
                }

                Image image = new Image();
                image.setRoom(new Room(roomId));
                image.setImageName(imageFiles.get(i).getOriginalFilename());
                image.setImageOriginalName(imageFiles.get(i).getOriginalFilename());
                image.setImagePath(savedFileName); // **이미지 경로가 없으면 저장하지 않음**

                String title = (imageTitles != null && i < imageTitles.size()) ? imageTitles.get(i) : "";
                String description = (imageDescriptions != null && i < imageDescriptions.size()) ? imageDescriptions.get(i) : "";
                image.setImageTitle(title);
                image.setImageDescription(description);

                // **이미지 경로가 존재하는 경우에만 저장**
                imageRepository.save(image);
                log.info("이미지 저장 완료 - 경로: {}, 제목: {}, 설명: {}", image.getImagePath(), image.getImageTitle(), image.getImageDescription());
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 데이터 저장 실패: " + e.getMessage(), e);
        }

        log.info(" [registerRoomImage] 호출됨 - Room ID: {}", roomId);
        return filenames; // 저장된 파일명 리스트 반환
    }


    // 객실 배너 이미지 등록
    public String registerRoombannerImage(MultipartFile file, Integer roomId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일을 리스트로 변환하여 업로드 실행
        List<MultipartFile> fileList = List.of(file);
        List<String> savedFilePaths = fileUpload.FileUpload(IMG_LOCATION, fileList);

        if (savedFilePaths == null || savedFilePaths.isEmpty() || savedFilePaths.get(0) == null) {
            throw new RuntimeException("파일 업로드 실패");
        }

        String fullFilePath = savedFilePaths.get(0); // 전체 경로 (예: c:/data/uuid.jpg)

        //  "c:/data/" 부분을 제거하고 파일명(UUID).jpg만 저장
        String savedFileName = fullFilePath.substring(fullFilePath.lastIndexOf("/") + 1);

        // 이미지 정보 저장
        Image image = new Image();
        image.setRoom(new Room(roomId));
        image.setImageName(file.getOriginalFilename());
        image.setImagePath(savedFileName); //  파일명(UUID).jpg만 저장
        image.setImageMain("Y"); // 대표 이미지 설정

        imageRepository.save(image);
        return savedFileName; //  파일명만 반환
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
//                    image.setImagePath(IMG_LOCATION + filename);
                    image.setImagePath("/upload/" + filename);
//                    image.setImagePath(filename);
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