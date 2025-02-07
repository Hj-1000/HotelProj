package com.ntt.ntt.Service.company;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.company.CompanyRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Util.FileUpload;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class CompanyService {

    //동적으로 경로를 설정하는 경우
    @Value("${file://c:/data/}")
    private String IMG_LOCATION;

    private final ImageRepository imageRepository;
    private final CompanyRepository companyRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Autowired
    private final ImageService imageService;
    @Autowired
    private FileUpload fileUpload;

    //등록
    public void register(CompanyDTO companyDTO, List<MultipartFile> imageFiles) {

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        Company company = modelMapper.map(companyDTO, Company.class);

        // 1. Company 먼저 저장
        companyRepository.save(company);

        // 2. imageFiles를 ImageService를 통해 저장
        imageService.registerCompanyImage(company.getCompanyId(), imageFiles);
    }



    //목록
    public Page<CompanyDTO> list(Pageable page, String keyword, String searchType) {

        // 1. 페이지 정보 재가공
        int currentPage = page.getPageNumber() - 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(
                currentPage, pageSize,
                Sort.by(Sort.Direction.DESC, "companyId")
        );

        // 2. 검색타입에 따른 회사 조회
        Page<Company> companies;

        if (keyword != null && !keyword.isEmpty()) {
            String keywordLike = "%" + keyword + "%";  // LIKE 조건을 위한 검색어 처리

            if ("name".equals(searchType)) {
                // 회사명에 검색어 포함된 경우 찾기
                companies = companyRepository.findByCompanyNameLike(keywordLike, pageable);
            } else if ("manager".equals(searchType)) {
                // 관리자명에 검색어 포함된 경우 찾기
                companies = companyRepository.findByCompanyManagerLike(keywordLike, pageable);
            } else if ("both".equals(searchType)) {
                // 회사명 또는 관리자명에 검색어 포함된 경우 찾기
                companies = companyRepository.findByCompanyNameLikeOrCompanyManagerLike(keywordLike, keywordLike, pageable);
            } else {
                // 기본적으로 이름과 관리자로 모두 검색
                companies = companyRepository.findByCompanyNameLikeOrCompanyManagerLike(keywordLike, keywordLike, pageable);
            }
        } else {
            // 검색어가 없으면 모든 회사 리스트를 조회
            companies = companyRepository.findAll(pageable);
        }

        // 3. Company -> CompanyDTO 변환 후 hotelCount 설정
        Page<CompanyDTO> companyDTOS = companies.map(entity -> {
            CompanyDTO companyDTO = modelMapper.map(entity, CompanyDTO.class);

            // 호텔 수를 구하기 위해 HotelRepository를 사용하여 해당 회사에 속한 호텔 수를 카운트
            int hotelCount = hotelRepository.countByCompany_CompanyId(companyDTO.getCompanyId());
            companyDTO.setHotelCount(hotelCount);

            return companyDTO;
        });

        return companyDTOS;
    }



    //개별보기
    @Transactional(readOnly = true)
    public CompanyDTO read(Integer companyId) {

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        // companyId를 통해 회사 정보 불러오기
        Optional<Company> company = companyRepository.findById(companyId);
        CompanyDTO companyDTO = modelMapper.map(company, CompanyDTO.class);

        //이미지 경로에서 c:/data/ 제거 코드 -> (파일명.파일확장자 만 빼오기 위해)
        //추후 더 간단하게 합쳐서 하나하나 입력할 필요 없이 다같이 사용 가능하도록 ImgService에 추가 할 예정
        List<ImageDTO> imgDTOList = imageRepository.findByCompany_CompanyId(companyDTO.getCompanyId())
                .stream().map(imagefile -> {
                    // 여기서 이미지 경로를 상대 경로로 변환
                    imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", ""));
                    return modelMapper.map(imagefile, ImageDTO.class);
                })
                .collect(Collectors.toList());

        companyDTO.setCompanyImgDTOList(imgDTOList);

        return companyDTO;

    }

    // 회사 정보 수정 (이미지 수정 포함)
    // 새로운 이미지 추가 안할때도 기존이미지가 자꾸 삭제됨 이건 추후 수정 예정 :2025-01-22
    @Transactional
    public void update(CompanyDTO companyDTO, List<MultipartFile> newImageFiles) {
        // 회사 조회 및 수정
        Optional<Company> companyOpt = companyRepository.findById(companyDTO.getCompanyId());
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();

            // 회사 정보 수정 (회사 이름과 관리자는 수정)
            company.setCompanyName(companyDTO.getCompanyName());
            company.setCompanyManager(companyDTO.getCompanyManager()); // 관리자가 추가된 부분

            // 회사 정보 저장 (이 부분에서 회사 이름과 관리자 저장)
            companyRepository.save(company);

            // 이미지 수정 처리
            if (newImageFiles != null && !newImageFiles.isEmpty()) {
                // 새 이미지가 있을 경우, 기존 이미지 삭제 및 새 이미지 업로드
                List<Image> existingImages = company.getCompanyImageList();  // 회사에 연결된 이미지들 가져오기

                // 기존 이미지 삭제 처리
                for (Image existingImage : existingImages) {
                    fileUpload.FileDelete(IMG_LOCATION, existingImage.getImageName());  // 파일 삭제
                    imageRepository.delete(existingImage);  // 이미지 엔티티 삭제
                }

                // 회사의 이미지 리스트에서 기존 이미지를 제거 (삭제된 이미지 목록)
                company.getCompanyImageList().clear();  // 기존 이미지 리스트 비우기

                // 새 이미지들 업로드 처리
                List<String> newFilenames = fileUpload.FileUpload(IMG_LOCATION, newImageFiles);

                // 업로드된 새 이미지들 저장
                for (int i = 0; i < newFilenames.size(); i++) {
                    Image newImage = new Image();
                    newImage.setImageName(newFilenames.get(i));
                    newImage.setImageOriginalName(newImageFiles.get(i).getOriginalFilename());
                    newImage.setImagePath(IMG_LOCATION + newFilenames.get(i));

                    // 이미지와 회사 관계 설정
                    newImage.setCompany(company);  // 이미지와 회사 연결

                    // 이미지 저장
                    imageRepository.save(newImage);

                    // 회사와 이미지 연결 (회사 정보에 이미지 추가)
                    company.getCompanyImageList().add(newImage);  // 이미지를 회사의 이미지 리스트에 추가
                }
            } else {
                // 새 이미지가 없으면 기존 이미지는 삭제하지 않고 그대로 둔다.
                // company.getCompanyImageList()는 그대로 유지됩니다.
            }

            // 최종 회사 정보와 이미지 정보 저장
            companyRepository.save(company);  // 회사 정보 저장 (변경된 회사 정보 및 연결된 이미지 저장)
        } else {
            throw new RuntimeException("본사를 찾을 수 없습니다.");
        }
    }



    // 회사 삭제
    @Transactional
    public void delete(Integer companyId) {
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();

            // 회사에 연결된 이미지 삭제
            List<Image> imagesToDelete = company.getCompanyImageList();
            for (Image image : imagesToDelete) {
                // 이미지 서비스에서 물리적 파일 삭제 + DB에서 삭제
                imageService.deleteImage(image.getImageId());
            }

            // 회사 삭제
            companyRepository.delete(company);
        } else {
            throw new RuntimeException("회사를 찾을 수 없습니다.");
        }
    }

    //    //기존 이미지 없을 때 버전
//    //수정
//    public void update(CompanyDTO companyDTO) {
//        //해당 데이터의 id로 조회
//        Optional<Company> company = companyRepository.findById(companyDTO.getCompanyId());
//        if (company.isPresent()) { //존재하면 수정
//            //변환
//            Company company1 = modelMapper.map(companyDTO, Company.class);
//            //저장
//            companyRepository.save(company1);
//        }
//    }
//
//
//    //삭제
//    public void delete(Integer companyId) {
//        companyRepository.deleteById(companyId);
//    }

    // 현재 로그인한 사용자가 등록한 본사 목록 가져오기
    public List<CompanyDTO> getFilteredCompany(Integer memberId) {
        List<Company> companies = companyRepository.findByMember_MemberId(memberId);

        return companies.stream()
                .map(company -> modelMapper.map(company, CompanyDTO.class))
                .collect(Collectors.toList());
    }

}
