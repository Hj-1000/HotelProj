package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.Entity.ServiceCate;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.ServiceCateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;



import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@jakarta.transaction.Transactional
@Log4j2
public class ServiceCateService{

    private final ImageRepository imageRepository;
    private final ServiceCateRepository serviceCateRepository;
    private final ModelMapper modelMapper;

    // 이미지 등록할 ImageService 의존성 추가
    private final ImageService imageService;
    //서비스 카테고리 등록
    /*--------------------------------
    함수명 : void insert(ServiceCateDTO seviceCateDTO, MultipartFile multipartFile)
    인수 : 조회할 메뉴 카테고리 번호
    출력 : 없음, 저장한 레코드 전달
    설명 : 전달받은 데이터를 데이터베이스에 저장
    --------------------------------*/
    public void register(ServiceCateDTO serviceCateDTO, List<MultipartFile> multipartFile){
        //1. 변환
        ServiceCate serviceCate =
                modelMapper.map(serviceCateDTO, ServiceCate.class);
        //2. ServiceCate 엔티티를 먼저 저장
        serviceCate = serviceCateRepository.save(serviceCate);
        log.info("저장된 ServiceCate: {}", serviceCate);
        //3. 저장된 serviceCateId를 사용하여 이미지 저장
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageService.registerServiceCateImage(serviceCate.getServiceCateId(), multipartFile);
        }
        log.info("잘 들어 왔나요?" + serviceCate);
        log.info("컨트롤러에서 들어온 이미지 정보" + multipartFile);
        log.info("ServiceCate object after mapping: {}", serviceCate);

//        log.info("저장된 serviceCate" + serviceCate);

    }
    //서비스 카테고리 목록
    /*--------------------------------
    함수명 : Page<ServiceCateDTO> list(Pageable page)
    인수 : 조회할 페이지 정보
    출력 : 해당 데이터들(list)과 page 정보를 전달
    설명 : 요청한 페이지번호에 해당하는 데이터를 조회해서 전달
    --------------------------------*/
    public Page<ServiceCateDTO> list(Pageable page){
        //1. 페이지정보를 재가공
        int currentPage = page.getPageNumber()-1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "serviceCateId"));

        //2. 조회
        Page<ServiceCate> serviceCatePage = serviceCateRepository.findAll(pageable);

        //3. 변환
        Page<ServiceCateDTO> serviceCateDTOS = serviceCatePage.map(entity ->modelMapper.map(entity, ServiceCateDTO.class));
        return serviceCateDTOS;
    }

    //서비스 카테고리 상세보기
/*--------------------------------
    함수명 : ServiceCateDTO(Integer serviceCateId)
    인수 : 조회할 메뉴 id 번호
    출력 : 조회할 데이터
    설명 : 해당 서비스 카테고리의 데이터를 조회해서 전달
    --------------------------------*/

    @Transactional(readOnly = true)
    public ServiceCateDTO read(Integer serviceCateId) {
        // 모델 매퍼 초기화 체크
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        // ServiceCate 개별 조회 및 예외 처리
        ServiceCate serviceCate =
                serviceCateRepository.findById(serviceCateId)
                        .orElseThrow(EntityNotFoundException::new);

        // 서비스 카테고리 DTO로 변환
        ServiceCateDTO serviceCateDTO = modelMapper.map(serviceCate, ServiceCateDTO.class);

        List<ImageDTO> imageDTOList = imageRepository.findByServiceCate_ServiceCateId(serviceCateDTO.getServiceCateId())
                .stream().map(imagefile -> {
                    //여기서 이미지 경로를 상대 경로로 변환
                    imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", ""));
                    return modelMapper.map(imagefile, ImageDTO.class);
                })
                .collect(Collectors.toList());

        serviceCateDTO.setServiceCateImageDTOList(imageDTOList);

        // 이미지 경로 추가 (첫 번째 이미지만 예시로 사용)
//        if (serviceCate.getServiceCateImageList() != null && !serviceCate.getServiceCateImageList().isEmpty()) {
//            serviceCateDTO.setImagePath("c:/data/" + serviceCate.getServiceCateImageList().get(0).getImageName());
//        }

        return serviceCateDTO;
    }


//    //서비스 카테로고리 수정
//    /*--------------------------------
//    함수명 : void update(ServiceCateDTO serviceCateDTO, MultipartFile multipartFile)
//    인수 : 조회할 메뉴 카테고리 번호
//    출력 : 없음, 저장한 레코드 전달
//    설명 : 전달받은 데이터를 데이터베이스에 저장
//    --------------------------------*/
//    public void update(ServiceCateDTO serviceCateDTO, MultipartFile multipartFile) {
//        //1. 변환
//        ServiceCate serviceCate = modelMapper.map(serviceCateDTO, ServiceCate.class);
//        //2. 이미지 파일 저장
//        if (!multipartFile.isEmpty()){
//            //3. 기존에 존재하는 이미지 파일이 있는지 확인 후 삭제
//            if (serviceCate.getServiceCateImg() != null) {
//                fileUpload.FileDelete(imgLocation, serviceCate.getServiceCateImg());
//            }
//            //4. 새로운 이미지 파일을 저장
//            String newImageName = fileUpload.FileUpload(imgLocation, multipartFile);
//            serviceCate.setServiceCateImg(newImageName);
//        }
//        serviceCateRepository.save(serviceCate);
//    }
//
//    public ServiceCate updateCheck(ServiceCateDTO serviceCateDTO, MultipartFile multipartFile) {
//        //1. 변환
//        ServiceCate serviceCate = modelMapper.map(serviceCateDTO, ServiceCate.class);
//        //2. 이미지 파일 저장
//        if (!multipartFile.isEmpty()){
//            //3. 기존에 존재하는 이미지 파일이 있는지 확인 후 삭제
//            if (serviceCate.getServiceCateImg() != null) {
//                fileUpload.FileDelete(imgLocation, serviceCate.getServiceCateImg());
//            }
//            //4. 새로운 이미지 파일을 저장
//            String newImageName = fileUpload.FileUpload(imgLocation, multipartFile);
//            serviceCate.setServiceCateImg(newImageName);
//        }
//        return serviceCateRepository.save(serviceCate);
//    }
//
//    //서비스 카테로고리 삭제
//    /*--------------------------------
//    함수명 : void delete(Integer serviceCateId)
//    인수 : 조회할 메뉴 카테고리 번호
//    출력 : 없음, 저장한 레코드 전달
//    설명 : 전달받은 데이터를 데이터베이스에 저장
//    --------------------------------*/
//    public void delete(Integer serviceCateId) {
//        serviceCateRepository.deleteById(serviceCateId);
//    }
//
//    // 결과값을 불리언으로 만들어서 제시
//    public boolean deleteCheck(Integer serviceCateId) {
//        serviceCateRepository.deleteById(serviceCateId);
//        Optional<ServiceCate> read = serviceCateRepository.findById(serviceCateId);
//        if (read.isPresent()) {
//            return false; //삭제 실패
//        } else {
//            return true; //삭제 성공
//        }
//    }
}

