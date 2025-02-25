package com.ntt.ntt.Service.company;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    private final ModelMapper modelMapper;

    @Autowired
    private final ImageService imageService;
    @Autowired
    private FileUpload fileUpload;

    //등록
    public void register(CompanyDTO companyDTO, List<MultipartFile> imageFiles, String memberEmail) {

        // 로그인한 회원 정보 조회
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("Member not found"));


        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        Company company = modelMapper.map(companyDTO, Company.class);

        //관리자명 로그인된 회원 이름으로
        company.setCompanyManager(member.getMemberName());

        // 🔹 회원 정보 설정 -> memberId 추가를 위해
        company.setMember(member);

        // 1. Company 먼저 저장
        companyRepository.save(company);
        // 2. imageFiles를 ImageService를 통해 저장
        imageService.registerCompanyImage(company.getCompanyId(), imageFiles);
        companyRepository.flush(); // 🔹 즉시 DB 반영하여 트랜잭션 지연 문제 방지
    }



    //목록
    public Page<CompanyDTO> listByAdmin(Pageable page, String keyword, String searchType) {

        // 1. 페이지 정보 재가공
        int currentPage = page.getPageNumber() - 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(
                currentPage, pageSize,
                Sort.by(Sort.Direction.ASC, "companyId")
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

            // 호텔에 대한 이미지 리스트 가져오기
            List<ImageDTO> imgDTOList = imageRepository.findByCompany_CompanyId(entity.getCompanyId())
                    .stream()
                    .map(imagefile -> {
                        imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", "")); // 경로 수정
                        return modelMapper.map(imagefile, ImageDTO.class);
                    })
                    .collect(Collectors.toList());

            // 호텔 수를 구하기 위해 HotelRepository를 사용하여 해당 회사에 속한 호텔 수를 카운트
            int hotelCount = hotelRepository.countByCompany_CompanyId(companyDTO.getCompanyId());
            companyDTO.setHotelCount(hotelCount);

            companyDTO.setCompanyImgDTOList(imgDTOList); // 이미지 DTO 리스트 설정
            return companyDTO;
        });

        return companyDTOS;
    }


    //목록
    public Page<CompanyDTO> listByChief(Pageable pageable, String keyword, String searchType, Integer memberId) {
        int currentPage = pageable.getPageNumber() - 1;
        int pageSize = 10;
        Pageable adjustedPageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "companyId"));

        Page<Company> companies;

        if (keyword != null && !keyword.isEmpty()) {
            String keywordLike = "%" + keyword + "%";

            if ("name".equals(searchType)) {
                companies = companyRepository.findByCompanyNameLikeAndMember_MemberId(keywordLike, memberId, adjustedPageable);
            } else if ("manager".equals(searchType)) {
                companies = companyRepository.findByCompanyManagerLikeAndMember_MemberId(keywordLike, memberId, adjustedPageable);
            } else if ("both".equals(searchType)) {
                companies = companyRepository.findByCompanyNameLikeOrCompanyManagerLikeAndMember_MemberId(keywordLike, keywordLike, memberId, adjustedPageable);
            } else {
                companies = companyRepository.findByCompanyNameLikeOrCompanyManagerLikeAndMember_MemberId(keywordLike, keywordLike, memberId, adjustedPageable);
            }
        } else {
            companies = companyRepository.findByMember_MemberId(memberId, adjustedPageable);
        }

        // Company -> CompanyDTO 변환 후 호텔 수 설정
        Page<CompanyDTO> companyDTOS = companies.map(entity -> {
            CompanyDTO companyDTO = modelMapper.map(entity, CompanyDTO.class);
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


    // companyId에 맞는 방들을 가져오는 메서드
    public Page<HotelDTO> hotelListBycompany(Integer companyId, Pageable page) {

        // 1. 페이지 정보 재가공
        int currentPage = page.getPageNumber(); // 기존 페이지 번호 그대로 사용
        int pageSize = page.getPageSize(); // 페이지 사이즈 그대로 사용
        Pageable pageable = PageRequest.of(
                currentPage, pageSize,
                Sort.by(Sort.Direction.ASC, "hotelId") // 등록순 정렬
        );

        // 2. 검색타입에 따른 호텔 조회
        Page<Hotel> hotels = null;
        hotels = hotelRepository.findByCompany_CompanyId(companyId, pageable);

        // 3. Hotel -> HotelDTO 변환
        Page<HotelDTO> hotelDTOS = hotels.map(entity -> {
            HotelDTO hotelDTO = modelMapper.map(entity, HotelDTO.class);

            // 호텔에 대한 이미지 리스트 가져오기
            List<ImageDTO> imgDTOList = imageRepository.findByHotel_HotelId(entity.getHotelId())
                    .stream()
                    .map(imagefile -> {
                        imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", "")); // 경로 수정
                        return modelMapper.map(imagefile, ImageDTO.class);
                    })
                    .collect(Collectors.toList());

            hotelDTO.setHotelImgDTOList(imgDTOList); // 이미지 DTO 리스트 설정
            return hotelDTO;
        });

        return hotelDTOS;
    }


    // 정보 수정 (이미지 추가 포함)
    @Transactional
    public void update(CompanyDTO companyDTO, List<MultipartFile> newImageFiles) {
        // 본사 조회 및 수정
        Optional<Company> companyOpt = companyRepository.findById(companyDTO.getCompanyId());
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();

            // 본사 정보 수정
            company.setCompanyName(companyDTO.getCompanyName());
            company.setCompanyManager(companyDTO.getCompanyManager());

            // 본사 정보 저장
            companyRepository.save(company);

            // 새 이미지 파일이 있을 경우 이미지 처리
            if (newImageFiles != null && !newImageFiles.isEmpty()) {
                // 새로운 이미지들 업로드
                List<String> newFilenames = fileUpload.FileUpload(IMG_LOCATION, newImageFiles);

                // 새 이미지들 저장
                for (int i = 0; i < newFilenames.size(); i++) {
                    Image newImage = new Image();
                    newImage.setImageName(newFilenames.get(i));
                    newImage.setImageOriginalName(newImageFiles.get(i).getOriginalFilename());
                    newImage.setImagePath(IMG_LOCATION + newFilenames.get(i));

                    // 이미지와 본사 관계 설정
                    newImage.setCompany(company);  // 이미지와 본사 연결
                    imageRepository.save(newImage);  // 이미지 저장

                    // 본사 객체에 새 이미지 추가
                    company.getCompanyImageList().add(newImage);
                }

                // 이미지 관계 업데이트 후 호텔 저장
                companyRepository.save(company);
            }
        } else {
            throw new RuntimeException("본사를 찾을 수 없습니다.");  // 본사를 찾을 수 없는 경우 예외 처리
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
