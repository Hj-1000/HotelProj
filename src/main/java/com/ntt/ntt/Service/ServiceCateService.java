package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.Entity.ServiceCate;
import com.ntt.ntt.Repository.ServiceCateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ServiceCateService{
    @Value("c:/roomServiceCategory/") //이미지가 저장될 위치
    private String imgLocation;
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
    public void register(ServiceCateDTO serviceCateDTO, MultipartFile multipartFile){
        //1. 변환
        ServiceCate serviceCate =
                modelMapper.map(serviceCateDTO, ServiceCate.class);
        //2. 이미지 파일 저장
        imageService.registerServiceCateImage(serviceCate.getServiceCateId(), multipartFile);
//        String newImageName = fileUpload.FileUpload(imgLocation, multipartFile);
//        serviceCate.setServiceCateImg(newImageName);
        log.info("잘 들어 왔나요?" + serviceCate);
        log.info("ServiceCate object after mapping: {}", serviceCate);
        serviceCateRepository.save(serviceCate);

    }
    //서비스 카테고리 목록
    /*--------------------------------
    함수명 : Page<ServiceCateDTO> list(Pageable page)
    인수 : 조회할 페이지 정보
    출력 : 해당 데이터들(list)과 page 정보를 전달
    설명 : 요청한 페이지번호에 해당하는 데이터를 조회해서 전달
    --------------------------------*/
//    public Page<ServiceCateDTO> list(Pageable page){
//        //1. 페이지정보를 재가공
//        int currentPage = page.getPageNumber()-1;
//        int pageSize = 10;
//        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "serviceCateId"));
//
//        //2. 조회
//        Page<ServiceCate> serviceCatePage = serviceCateRepository.findAll(pageable);
//
//        //3. 변환
//        Page<ServiceCateDTO> serviceCateDTOS = serviceCatePage.map(entity ->modelMapper.map(entity, ServiceCateDTO.class));
//        return serviceCateDTOS;
//    }
//
//    //서비스 카테고리 상세보기
///*--------------------------------
//    함수명 : ServiceCateDTO(Integer serviceCateId)
//    인수 : 조회할 메뉴 id 번호
//    출력 : 조회할 데이터
//    설명 : 해당 서비스 카테고리의 데이터를 조회해서 전달
//    --------------------------------*/
//    public ServiceCateDTO read(Integer serviceCateId) {
//        ServiceCate serviceCate =
//                serviceCateRepository.findById(serviceCateId).orElseThrow(EntityNotFoundException::new);
//
//        ServiceCateDTO serviceCateDTO =
//                modelMapper.map(serviceCate, ServiceCateDTO.class);
//
//        return serviceCateDTO;
//    }
//
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

