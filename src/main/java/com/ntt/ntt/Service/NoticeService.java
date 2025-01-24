package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Util.FileUpload;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ntt.ntt.Entity.Notice;
import com.ntt.ntt.DTO.NoticeDTO;
import com.ntt.ntt.Repository.NoticeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2

public class NoticeService {

    private final ImageRepository imageRepository;

    private final NoticeRepository noticeRepository;

    private final ModelMapper modelMapper;

    // 이미지 등록할 ImageService 의존성
    private final ImageService imageService;
    private final FileUpload fileUpload;

    public Page<NoticeDTO> getNotices(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return noticeRepository.findAll(pageRequest).map(notice -> new NoticeDTO(notice));
    }

    @Value("${dataUploadPath}")
    private String IMG_LOCATION;

    public void register(NoticeDTO noticeDTO, List<MultipartFile> multipartFile) {
        Notice notice =
                modelMapper.map(noticeDTO, Notice.class);
        //노티스 글 먼저 저장
        noticeRepository.save(notice);
        if (multipartFile !=null && !multipartFile.isEmpty()){
            imageService.registerNoticeImage(notice.getNoticeId(), multipartFile);
        }
    }

    public void update(NoticeDTO noticeDTO, List<MultipartFile> multipartFile) {

        Optional<Notice> notice = noticeRepository.findById(noticeDTO.getNoticeId());
        if (notice.isPresent()) {
            Notice notice1 = modelMapper.map(noticeDTO, Notice.class);
            noticeRepository.save(notice1);
        } else {
            Notice notice1 = modelMapper.map(noticeDTO, Notice.class);
            noticeRepository.save(notice1);
        }

    }
    public List<NoticeDTO> list() {
        List<Notice> noticeList = noticeRepository.findAll();
        List<NoticeDTO> noticeDTOList = Arrays.asList(modelMapper.map(noticeList, NoticeDTO[].class));
        return noticeDTOList;
    }
    public NoticeDTO read(Integer noticeId) {
        Optional<Notice> notice = noticeRepository.findById(noticeId);
        NoticeDTO noticeDTO = modelMapper.map(notice, NoticeDTO.class);
        List<ImageDTO> imageDTOList = imageRepository.findByNotice_NoticeId(noticeDTO.getNoticeId())
                .stream().map(imagefile -> {
                    //여기서 이미지 경로를 상대 경로로 변환
                    imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", ""));
                    return modelMapper.map(imagefile, ImageDTO.class);
                })
                .collect(Collectors.toList());

        noticeDTO.setNoticeImageDTOList(imageDTOList);

        return noticeDTO;
    }

    public void delete(Integer noticeId) {
        Optional<Notice> noticeOpt = noticeRepository.findById(noticeId);
        if (noticeOpt.isPresent()) {
            Notice notice = noticeOpt.get();

            List<Image> imagesToDelete = notice.getNoticeImageList();
            for (Image image : imagesToDelete) {

                imageService.deleteImage(image.getImageId());
            }
            noticeRepository.delete(notice);
        }else{
            throw new RuntimeException("공지사항이 없습니다");
        }
    }

    public List<NoticeDTO> getFilteredNotice(String noticeTitle, String noticeContent,String regDate, String modDate) {

        List<Notice> notices = noticeRepository.findAll();

        if (noticeTitle != null && !noticeTitle.isEmpty()) {
            notices = notices.stream()
                    .filter(notice -> notice.getNoticeTitle().contains(noticeTitle))
                    .collect(Collectors.toList());
        }
        if (noticeContent != null && !noticeContent.isEmpty()) {
            notices = notices.stream()
                    .filter(notice -> notice.getNoticeContent().contains(noticeContent))
                    .collect(Collectors.toList());
        }


//        if (regDate != null && !regDate.isEmpty() && modDate != null && !modDate.isEmpty()) {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            LocalDate start = LocalDate.parse(regDate, formatter);
//            LocalDate end = LocalDate.parse(modDate, formatter);
//
//            notices = notices.stream()
//                    .filter(notice -> {
//                        LocalDate createdDate = notice.getRegDate().toLocalDate();
//                        return (createdDate.isEqual(start) || createdDate.isAfter(start)) &&
//                                (createdDate.isEqual(end) || createdDate.isBefore(end));
//                    })
//                    .collect(Collectors.toList());
//        }

        return notices.stream()
                .map(notice -> modelMapper.map(notice, NoticeDTO.class))
                .collect(Collectors.toList());
    }

//    public Notice readByNoticeTitle(String noticeTitle) {
//        Optional<Notice> notice = noticeRepository.findByNoticeTitle(noticeTitle);
//        return notice.orElse(null);  // 없으면 null 반환
//    }

//    public void delete(Integer noticeId, List<MultipartFile> multipartFile) {
//        noticeRepository.deleteById(noticeId);
//        //파일삭제 추가
//        if (multipartFile != null && !multipartFile.isEmpty()) {
//            for (MultipartFile file : multipartFile) {
//                try {
//                    Path path = Paths.get(IMG_LOCATION + file.getOriginalFilename());
//
//                    Files.deleteIfExists(path);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//public void delete(Integer noticeId, List<MultipartFile> multipartFile) {
//    // Notice 엔터티 조회
//    Notice notice = noticeRepository.findById(noticeId)
//            .orElseThrow(() -> new RuntimeException("Notice not found"));
//
//    // 파일 삭제
//    if (notice.getNoticeImageList() != null && !notice.getNoticeImageList().isEmpty()) {
//        for (Image image : notice.getNoticeImageList()) {
//            try {
//                // 경로 생성 및 파일 존재 여부 확인
//                Path path = Paths.get(IMG_LOCATION, image.getImageName());
//                System.out.println("Deleting file: " + path.toString());
//                if (Files.exists(path)) {
//                    Files.delete(path);
//                } else {
//                    System.out.println("File does not exist: " + path.toString());
//                }
//            } catch (IOException e) {
//                System.err.println("Failed to delete file: " + image.getImageName());
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // Notice 삭제 (cascade 설정으로 연관된 이미지도 삭제)
//    noticeRepository.delete(notice);
//}

//        for (MultipartFile multipartFile : multipartFile) {
//            Path filePath = Paths.get(IMG_LOCATION + multipartFile.getOriginalFilename());
//            try {
//                multipartFile.delete(MultipartFile.class);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }




//    이미지 등록
//    public void saveImage(Integer imageId, List<MultipartFile> multipartFiles) throws IOException{
//
//        if(multipartFiles != null){
//            for(MultipartFile image: multipartFiles){
//                if(!image.isEmpty()){
//                    String savedFileName = ImageService.fileUpload(image);
//                }
//            }
//        }
//    }


}
