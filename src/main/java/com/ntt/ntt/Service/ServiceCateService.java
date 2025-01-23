package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Entity.ServiceCate;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.ServiceCateRepository;
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
public class ServiceCateService{

    private final ImageRepository imageRepository;
    private final ServiceCateRepository serviceCateRepository;
    private final ServiceMenuRepository serviceMenuRepository;
    private final ModelMapper modelMapper;

    // 이미지 등록할 ImageService 의존성 추가
    private final ImageService imageService;
    private final FileUpload fileUpload;

    @Value("${dataUploadPath}")
    private String IMG_LOCATION;

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



    }
    //서비스 카테고리 목록
    /*--------------------------------
    함수명 : Page<ServiceCateDTO> list(Pageable page)
    인수 : 조회할 페이지 정보
    출력 : 해당 데이터들(list)과 page 정보를 전달
    설명 : 요청한 페이지번호에 해당하는 데이터를 조회해서 전달
    --------------------------------*/
    public Page<ServiceCateDTO> list(Pageable page, String keyword, String searchType) {
        // 1. 페이지정보를 재가공
        int currentPage = page.getPageNumber() - 1; // 0-based index
        int pageSize = 10;
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "serviceCateId"));

        // 2. 조회
        Page<ServiceCate> serviceCatePage;

        if (keyword != null && !keyword.isEmpty()) {
            // 검색어가 있을 경우
            String keywordLike = "%" + keyword + "%";
            if ("name".equals(searchType)) {
                // 카테고리명에 검색어 포함된 경우 찾기
                serviceCatePage = serviceCateRepository.findByServiceCateNameLike(keywordLike, pageable);
            } else {
                // 다른 검색 조건이 있을 경우 처리
                // 예시: 'searchType'에 따른 추가적인 로직 구현 가능
                serviceCatePage = serviceCateRepository.findAll(pageable); // 예시: 모든 항목 조회
            }
        } else {
            // 검색어가 없을 경우 모든 카테고리 조회
            serviceCatePage = serviceCateRepository.findAll(pageable);
        }

        // 3. 변환
        Page<ServiceCateDTO> serviceCateDTOS = serviceCatePage.map(entity -> modelMapper.map(entity, ServiceCateDTO.class));

        return serviceCateDTOS;
    }

    //존재하는 카테고리 목록 가져오기
    /*--------------------------------
    함수명 : List<ServiceCateDTO> getAllServiceCate()
    인수 : 조회할 메뉴 id 번호
    출력 : 조회할 데이터
    설명 : 해당 서비스 카테고리의 데이터를 조회해서 전달
    --------------------------------*/
    public List<ServiceCateDTO> getAllServiceCate() {
        List<ServiceCate> serviceCate = serviceCateRepository.findAll();

        List<ServiceCateDTO> serviceCateDTOS = serviceCate.stream()
                .map(a -> new ServiceCateDTO(a.getServiceCateId(), a.getServiceCateName())).collect(Collectors.toList());

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


    //서비스 카테로고리 수정
    /*--------------------------------
    함수명 : void update(ServiceCateDTO serviceCateDTO, MultipartFile multipartFile)
    인수 : 조회할 메뉴 카테고리 번호
    출력 : 없음, 저장한 레코드 전달
    설명 : 전달받은 데이터를 데이터베이스에 저장
    --------------------------------*/
    // 수정 메서드 최종 수정
    public void update(ServiceCateDTO serviceCateDTO, List<MultipartFile> multipartFile) {
        // 기존에 등록된 데이터 pk값으로 찾기
        Optional<ServiceCate> serviceCateOpt = serviceCateRepository.findById(serviceCateDTO.getServiceCateId());

        if (serviceCateOpt.isPresent()) {
            ServiceCate serviceCate = serviceCateOpt.get();
            // ServiceCate 정보 업데이트
            serviceCate.setServiceCateName(serviceCateDTO.getServiceCateName());
            serviceCateRepository.save(serviceCate); // 카테고리 정보 업데이트

            // 기존 이미지들 삭제 및 새 이미지 등록 (있다면)
            if (multipartFile != null && !multipartFile.isEmpty()) {
                // 기존 이미지들을 삭제하고 새 이미지를 업로드
                List<Image> existingImages = serviceCate.getServiceCateImageList();
                // 기존 이미지를 삭제
                for (Image existingImage : existingImages) {
                    log.info(existingImage.getImageId());
                    imageRepository.delete(existingImage);

                    log.info("기존이미지를 잘 삭제했나?");
                    log.info(existingImage);
                }
                //회사의 이미지 리스트에서 삭제된 이미지 제거
                serviceCate.getServiceCateImageList().clear(); // 이미지 모두 제거

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
                    newImage.setServiceCate(serviceCate);  // 이미지와 회사 연결

                    // 이미지 저장
                    imageRepository.save(newImage);

                    // 회사와 이미지 연결 (회사 정보에 이미지 추가)
                    serviceCate.getServiceCateImageList().add(newImage);  // 이미지를 회사의 이미지 리스트에 추가
                }

                // 새 이미지 등록
                serviceCateRepository.save(serviceCate);
            }

            log.info("서비스 카테고리 및 이미지가 수정되었습니다.");
        } else {
            throw new RuntimeException("서비스 카테고리를 찾을 수 없습니다.");
        }
    }


    //서비스 카테로고리 삭제
    /*--------------------------------
    함수명 : void delete(Integer serviceCateId)
    인수 : 조회할 메뉴 카테고리 번호
    출력 : 없음, 저장한 레코드 전달
    설명 : 전달받은 데이터를 데이터베이스에 저장
    --------------------------------*/
    public void delete(Integer serviceCateId) {
        Optional<ServiceCate> read = serviceCateRepository.findById(serviceCateId);


        if (read.isPresent()) {
            ServiceCate serviceCate = read.get();

            // 카테고리와 연결된 이미지 삭제
            List<Image> imagesDeleteAll = serviceCate.getServiceCateImageList();

            for (Image image : imagesDeleteAll) {
                //이미지를 물리적 파일 삭제 + DB에서도 삭제
                imageService.deleteImage(image.getImageId());
            }
            //위 과정을 모두 진행했다면 카테고리를 삭제
//            serviceMenuRepository.delete(serviceMenu);
            serviceCateRepository.delete(serviceCate);
        } else {
            throw new RuntimeException("카테고리를 찾을 수 없습니다");
        }

    }

    // 결과값을 불리언으로 만들어서 제시
    public boolean deleteCheck(Integer serviceCateId) {
        serviceCateRepository.deleteById(serviceCateId);
        Optional<ServiceCate> read = serviceCateRepository.findById(serviceCateId);
        if (read.isPresent()) {
            return false; //삭제 실패
        } else {
            return true; //삭제 성공
        }
    }
}

