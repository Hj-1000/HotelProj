package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.ServiceMenuDTO;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.ServiceMenuRepository;
import com.ntt.ntt.Util.FileUpload;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@jakarta.transaction.Transactional
@Log4j2
public class ServiceMenuService {

    private final ImageRepository imageRepository;
    private final ServiceMenuRepository serviceMenuRepository;
    private final ModelMapper modelMapper;

    // 이미지 등록할 ImageService 의존성 추가
    private final ImageService imageService;
    private final FileUpload fileUpload;

    @Value("${dataUploadPath}")
    private String IMG_LOCATION;

    //서비스 카테고리 등록
    /*--------------------------------
    함수명 : void insert(serviceMenuDTO seviceCateDTO, MultipartFile multipartFile)
    인수 : 조회할 메뉴 카테고리 번호
    출력 : 없음, 저장한 레코드 전달
    설명 : 전달받은 데이터를 데이터베이스에 저장
    --------------------------------*/
    public void register(ServiceMenuDTO serviceMenuDTO, List<MultipartFile> multipartFile){
        //1. 변환
        ServiceMenu serviceMenu =
                modelMapper.map(serviceMenuDTO, ServiceMenu.class);
        //2. serviceMenu 엔티티를 먼저 저장
        serviceMenu = serviceMenuRepository.save(serviceMenu);
        log.info("저장된 serviceMenu: {}", serviceMenu);
        //3. 저장된 serviceMenuId를 사용하여 이미지 저장
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageService.registerServiceMenuImage(serviceMenu.getServiceMenuId(), multipartFile);
        }
        log.info("잘 들어 왔나요?" + serviceMenu);
        log.info("컨트롤러에서 들어온 이미지 정보" + multipartFile);
        log.info("serviceMenu object after mapping: {}", serviceMenu);



    }
    //서비스 카테고리 목록
    /*--------------------------------
    함수명 : Page<serviceMenuDTO> list(Pageable page)
    인수 : 조회할 페이지 정보
    출력 : 해당 데이터들(list)과 page 정보를 전달
    설명 : 요청한 페이지번호에 해당하는 데이터를 조회해서 전달
    --------------------------------*/
    public Page<ServiceMenuDTO> list(Pageable page){
        //1. 페이지정보를 재가공
        int currentPage = page.getPageNumber()-1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "serviceMenuId"));

        //2. 조회
        Page<ServiceMenu> serviceMenuPage = serviceMenuRepository.findAll(pageable);

        //3. 변환
        Page<ServiceMenuDTO> serviceMenuDTOS = serviceMenuPage.map(entity ->modelMapper.map(entity, ServiceMenuDTO.class));
        return serviceMenuDTOS;
    }

    //서비스 카테고리 상세보기
/*--------------------------------
    함수명 : serviceMenuDTO(Integer serviceMenuId)
    인수 : 조회할 메뉴 id 번호
    출력 : 조회할 데이터
    설명 : 해당 서비스 카테고리의 데이터를 조회해서 전달
    --------------------------------*/

    @Transactional(readOnly = true)
    public ServiceMenuDTO read(Integer serviceMenuId) {
        // 모델 매퍼 초기화 체크
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        // serviceMenu 개별 조회 및 예외 처리
        ServiceMenu serviceMenu =
                serviceMenuRepository.findById(serviceMenuId)
                        .orElseThrow(EntityNotFoundException::new);

        // 서비스 카테고리 DTO로 변환
        ServiceMenuDTO serviceMenuDTO = modelMapper.map(serviceMenu, ServiceMenuDTO.class);

        List<ImageDTO> imageDTOList = imageRepository.findByServiceMenu_ServiceMenuId(serviceMenuDTO.getServiceMenuId())
                .stream().map(imagefile -> {
                    //여기서 이미지 경로를 상대 경로로 변환
                    imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", ""));
                    return modelMapper.map(imagefile, ImageDTO.class);
                })
                .collect(Collectors.toList());

        serviceMenuDTO.setServiceMenuImageDTOList(imageDTOList);

        // 이미지 경로 추가 (첫 번째 이미지만 예시로 사용)
//        if (serviceMenu.getserviceMenuImageList() != null && !serviceMenu.getserviceMenuImageList().isEmpty()) {
//            serviceMenuDTO.setImagePath("c:/data/" + serviceMenu.getserviceMenuImageList().get(0).getImageName());
//        }

        return serviceMenuDTO;
    }


    //서비스 카테로고리 수정
    /*--------------------------------
    함수명 : void update(serviceMenuDTO serviceMenuDTO, MultipartFile multipartFile)
    인수 : 조회할 메뉴 카테고리 번호
    출력 : 없음, 저장한 레코드 전달
    설명 : 전달받은 데이터를 데이터베이스에 저장
    --------------------------------*/
    // 수정 메서드 최종 수정
    public void update(ServiceMenuDTO serviceMenuDTO, List<MultipartFile> multipartFile) {
        // 기존에 등록된 데이터 pk값으로 찾기
        Optional<ServiceMenu> serviceMenuOpt = serviceMenuRepository.findById(serviceMenuDTO.getServiceMenuId());

        if (serviceMenuOpt.isPresent()) {
            ServiceMenu serviceMenu = serviceMenuOpt.get();
            // serviceMenu 정보 업데이트
            serviceMenu.setServiceMenuName(serviceMenuDTO.getServiceMenuName());
            serviceMenuRepository.save(serviceMenu); // 카테고리 정보 업데이트

            // 기존 이미지들 삭제 및 새 이미지 등록 (있다면)
            if (multipartFile != null && !multipartFile.isEmpty()) {
                // 기존 이미지들을 삭제하고 새 이미지를 업로드
                List<Image> existingImages = serviceMenu.getServiceMenuImageList();
                // 기존 이미지를 삭제
                for (Image existingImage : existingImages) {
                    log.info(existingImage.getImageId());
                    imageRepository.delete(existingImage);

                    log.info("기존이미지를 잘 삭제했나?");
                    log.info(existingImage);
                }
                //회사의 이미지 리스트에서 삭제된 이미지 제거
                serviceMenu.getServiceMenuImageList().clear(); // 이미지 모두 제거

                // 새 이미지들 업로드 처리
                List<String> newFilenames = fileUpload.FileUpload(IMG_LOCATION, multipartFile);
                if (newFilenames == null || newFilenames.isEmpty()) {
                    throw new RuntimeException("파일 업로드 실패");
                }

                // 업로드된 새 이미지들 저장
                for (int i = 0; i < newFilenames.size(); i++) {
                    Image newImage = new Image();
                    newImage.setImageName(newFilenames.get(i));
                    newImage.setImageOriginalName(multipartFile.get(i).getOriginalFilename());
                    newImage.setImagePath(IMG_LOCATION + newFilenames.get(i));

                    // 이미지와 회사 관계 설정
                    newImage.setServiceMenu(serviceMenu);  // 이미지와 회사 연결

                    // 이미지 저장
                    imageRepository.save(newImage);

                    // 회사와 이미지 연결 (회사 정보에 이미지 추가)
                    serviceMenu.getServiceMenuImageList().add(newImage);  // 이미지를 회사의 이미지 리스트에 추가
                }

                // 새 이미지 등록
                serviceMenuRepository.save(serviceMenu);
            }

            log.info("서비스 카테고리 및 이미지가 수정되었습니다.");
        } else {
            throw new RuntimeException("서비스 카테고리를 찾을 수 없습니다.");
        }
    }


    //서비스 카테로고리 삭제
    /*--------------------------------
    함수명 : void delete(Integer serviceMenuId)
    인수 : 조회할 메뉴 카테고리 번호
    출력 : 없음, 저장한 레코드 전달
    설명 : 전달받은 데이터를 데이터베이스에 저장
    --------------------------------*/
    public void delete(Integer serviceMenuId) {
        serviceMenuRepository.deleteById(serviceMenuId);
    }

    // 결과값을 불리언으로 만들어서 제시
    public boolean deleteCheck(Integer serviceMenuId) {
        serviceMenuRepository.deleteById(serviceMenuId);
        Optional<ServiceMenu> read = serviceMenuRepository.findById(serviceMenuId);
        if (read.isPresent()) {
            return false; //삭제 실패
        } else {
            return true; //삭제 성공
        }
    }
}

