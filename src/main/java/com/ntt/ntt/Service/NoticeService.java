package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Util.FileUpload;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ntt.ntt.Entity.Notice;
import com.ntt.ntt.DTO.NoticeDTO;
import com.ntt.ntt.Repository.NoticeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;


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

    // 이미지 등록할 ImageService 의존성 추가
    private final ImageService imageService;
    private final FileUpload fileUpload;

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
    public void delete(Integer noticeId) {
        noticeRepository.deleteById(noticeId);
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
