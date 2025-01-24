package com.ntt.ntt.Service.hotel;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.company.CompanyRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Util.FileUpload;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelService {
    //동적으로 경로를 설정하는 경우
    @Value("${file://c:/data/}")
    private String IMG_LOCATION;

    private final ImageRepository imageRepository;

    private final CompanyRepository companyRepository;

    private final ModelMapper modelMapper;

    @Autowired
    private final ImageService imageService;
    @Autowired
    private FileUpload fileUpload;

    //호텔 레포지토리
    private final HotelRepository hotelRepository;


    //호텔 본사 불러오는
    public List<CompanyDTO> getAllCompany() {
        List<Company> companies = companyRepository.findAll();

        List<CompanyDTO> companyDTOS = companies.stream()
                .map(a -> new CompanyDTO(a.getCompanyId(), a.getCompanyName())).collect(Collectors.toList());

        return companyDTOS;
    }


    //등록
    public void register(HotelDTO hotelDTO, List<MultipartFile> imageFiles) {

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        Hotel hotel = modelMapper.map(hotelDTO, Hotel.class);

//        hotel.setCompany(companyRepository.findById(hotelDTO.getHotelId()).orElseThrow());



        // 1. Hotel 먼저 저장
        hotelRepository.save(hotel);

        // 2. imageFiles를 ImageService를 통해 저장
        imageService.registerHotelImage(hotel.getHotelId(), imageFiles);
    }


    //본사관리자용목록
    public Page<HotelDTO> listByCompany(Pageable page, String keyword, Integer keyword1, String searchType, Integer companyId) {
        // 1. 페이지 정보 재가공
        int currentPage = page.getPageNumber(); // 기존 페이지 번호 그대로 사용
        int pageSize = page.getPageSize(); // 페이지 사이즈 그대로 사용
        Pageable pageable = PageRequest.of(
                currentPage, pageSize,
                Sort.by(Sort.Direction.DESC, "hotelId") // 최신순으로 정렬
        );

        // 2. 검색타입에 따른 호텔 조회
        Page<Hotel> hotels = null;

        if (companyId != null) {
            // companyId가 있을 경우 해당 회사의 호텔만 조회
            if (keyword != null && !keyword.isEmpty()) {
                String keywordLike = "%" + keyword + "%";  // LIKE 조건을 위한 검색어 처리

                // 검색 타입에 따라 조건을 추가
                if ("name".equals(searchType)) {
                    // 호텔명 검색
                    hotels = hotelRepository.findByCompany_CompanyIdAndHotelNameLike(companyId, keywordLike, pageable);
                } else if ("location".equals(searchType)) {
                    // 지역 검색
                    hotels = hotelRepository.findByCompany_CompanyIdAndHotelLocationLike(companyId, keywordLike, pageable);
                } else if ("address".equals(searchType)) {
                    // 주소 검색
                    hotels = hotelRepository.findByCompany_CompanyIdAndHotelAddressLike(companyId, keywordLike, pageable);
                } else if ("rating".equals(searchType)) {
                    // 별점 검색
                    hotels = hotelRepository.findByCompany_CompanyIdAndHotelRating(companyId, keyword1, pageable);
                }
            } else {
                // 검색어가 없으면 해당 companyId에 속한 모든 호텔 조회
                hotels = hotelRepository.findByCompany_CompanyId(companyId, pageable);
            }
        } else {
            // companyId가 없으면 모든 호텔 조회
            if (keyword != null && !keyword.isEmpty()) {
                String keywordLike = "%" + keyword + "%";  // LIKE 조건을 위한 검색어 처리

                // 검색 타입에 따라 조건을 추가
                if ("name".equals(searchType)) {
                    hotels = hotelRepository.findByHotelNameLike(keywordLike, pageable);
                } else if ("location".equals(searchType)) {
                    hotels = hotelRepository.findByHotelLocationLike(keywordLike, pageable);
                } else if ("address".equals(searchType)) {
                    hotels = hotelRepository.findByHotelAddressLike(keywordLike, pageable);
                } else if ("rating".equals(searchType)) {
                    hotels = hotelRepository.findByHotelRating(keyword1, pageable);
                }
            } else {
                // 검색어가 없으면 모든 호텔 리스트를 조회
                hotels = hotelRepository.findAll(pageable);
            }
        }

        // 3. Hotel -> HotelDTO 변환
        Page<HotelDTO> hotelDTOS = hotels.map(entity -> modelMapper.map(entity, HotelDTO.class));

        return hotelDTOS;
    }

//    public Page<HotelDTO> listByCompany(Pageable page, String keyword, Integer keyword1, String searchType, Integer companyId) {
//
//        // 1. 페이지 정보 재가공
//        int currentPage = page.getPageNumber() - 1;
//        int pageSize = 10;
//        Pageable pageable = PageRequest.of(
//                currentPage, pageSize,
//                Sort.by(Sort.Direction.DESC, "hotelId")
//        );
//
//        // 2. 검색타입에 따른 호텔 조회
//        Page<Hotel> hotels = null;
//
//        // companyId가 있을 경우 해당 회사의 호텔만 조회
//        if (companyId != null) {
//            // companyId가 있으면 회사에 속한 호텔만 조회
//            if (keyword != null && !keyword.isEmpty()) {
//                String keywordLike = "%" + keyword + "%";  // LIKE 조건을 위한 검색어 처리
//
//                // 검색 타입에 따라 조건을 추가
//                if ("name".equals(searchType)) {
//                    // 호텔명 검색
//                    hotels = hotelRepository.findByCompany_CompanyIdAndHotelNameLike(companyId, keywordLike, pageable);
//                } else if ("location".equals(searchType)) {
//                    // 지역 검색
//                    hotels = hotelRepository.findByCompany_CompanyIdAndHotelLocationLike(companyId, keywordLike, pageable);
//                } else if ("address".equals(searchType)) {
//                    // 주소 검색
//                    hotels = hotelRepository.findByCompany_CompanyIdAndHotelAddressLike(companyId, keywordLike, pageable);
//                } else if ("rating".equals(searchType)) {
//                    // 별점 검색
//                    hotels = hotelRepository.findByCompany_CompanyIdAndHotelRating(companyId, keyword1, pageable);
//                }
//            } else {
//                // 검색어가 없으면 해당 companyId에 속한 모든 호텔 조회
//                hotels = hotelRepository.findByCompany_CompanyId(companyId, pageable);
//            }
//        } else {
//            // companyId가 없으면 모든 호텔 조회
//            if (keyword != null && !keyword.isEmpty()) {
//                String keywordLike = "%" + keyword + "%";  // LIKE 조건을 위한 검색어 처리
//
//                // 검색 타입에 따라 조건을 추가
//                if ("name".equals(searchType)) {
//                    hotels = hotelRepository.findByHotelNameLike(keywordLike, pageable);
//                } else if ("location".equals(searchType)) {
//                    hotels = hotelRepository.findByHotelLocationLike(keywordLike, pageable);
//                } else if ("address".equals(searchType)) {
//                    hotels = hotelRepository.findByHotelAddressLike(keywordLike, pageable);
//                } else if ("rating".equals(searchType)) {
//                    hotels = hotelRepository.findByHotelRating(keyword1, pageable);
//                }
//            } else {
//                // 검색어가 없으면 모든 호텔 리스트를 조회
//                hotels = hotelRepository.findAll(pageable);
//            }
//        }
//
//        // 3. Hotel -> HotelDTO 변환
//        Page<HotelDTO> hotelDTOS = hotels.map(entity -> modelMapper.map(entity, HotelDTO.class));
//
//        return hotelDTOS;
//    }

    //일반회원 목록
    public Page<HotelDTO> list(Pageable page, String keyword, String searchType, boolean exactMatch) {

        int currentPage = page.getPageNumber();  // Page.getPageNumber()는 0부터 시작
        int pageSize = 9; // 한 페이지에 9개씩 표시
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "hotelId"));

        Page<Hotel> hotels = null;

        if (keyword != null && !keyword.isEmpty()) {
            if ("name".equals(searchType)) {
                // 호텔명이 포함된 경우
                hotels = hotelRepository.findByHotelNameLike("%" + keyword + "%", pageable);
            } else if ("location".equals(searchType)) {
                // 정확히 일치하는 location 검색
                if (exactMatch) {
                    hotels = hotelRepository.findByHotelLocationEquals(keyword, pageable);
                } else {
                    // location에서 %가 포함되도록
                    hotels = hotelRepository.findByHotelLocationLike("%" + keyword + "%", pageable);
                }
            } else if ("address".equals(searchType)) {
                // 주소 검색
                hotels = hotelRepository.findByHotelAddressLike("%" + keyword + "%", pageable);
            } else if ("rating".equals(searchType)) {
                // 별점 검색
                hotels = hotelRepository.findByHotelRating(Integer.parseInt(keyword), pageable);
            }
        } else {
            // 검색어가 없으면 모든 호텔 리스트를 조회
            hotels = hotelRepository.findAll(pageable);
        }

        // Hotel -> HotelDTO 변환
        Page<HotelDTO> hotelDTOS = hotels.map(entity -> modelMapper.map(entity, HotelDTO.class));

        return hotelDTOS;
    }


    //개별보기
    @Transactional(readOnly = true)
    public HotelDTO read(Integer hotelId) {

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        // hotelId를 통해 회사 정보 불러오기
        Optional<Hotel> hotel = hotelRepository.findById(hotelId);
        HotelDTO hotelDTO = modelMapper.map(hotel, HotelDTO.class);

        //이미지 경로에서 c:/data/ 제거 코드 -> (파일명.파일확장자 만 빼오기 위해)
        //추후 더 간단하게 합쳐서 하나하나 입력할 필요 없이 다같이 사용 가능하도록 ImgService에 추가 할 예정
        List<ImageDTO> imgDTOList = imageRepository.findByHotel_HotelId(hotelDTO.getHotelId())
                .stream().map(imagefile -> {
                    // 여기서 이미지 경로를 상대 경로로 변환
                    imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", ""));
                    return modelMapper.map(imagefile, ImageDTO.class);
                })
                .collect(Collectors.toList());

        hotelDTO.setHotelImgDTOList(imgDTOList);

        return hotelDTO;

    }

    // 정보 수정 (이미지 수정 포함)
    @Transactional
    public void update(HotelDTO hotelDTO, List<MultipartFile> newImageFiles) {
        // 조회 및 수정
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelDTO.getHotelId());
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            hotel.setHotelName(hotelDTO.getHotelName());
            hotel.setHotelLocation(hotelDTO.getHotelLocation());
            hotel.setHotelAddress(hotelDTO.getHotelAddress());
            hotel.setHotelEmail(hotelDTO.getHotelEmail());
            hotel.setHotelPhone(hotelDTO.getHotelPhone());
            hotel.setHotelInfo(hotelDTO.getHotelInfo());
            hotel.setHotelCheckIn(hotelDTO.getHotelCheckIn());
            hotel.setHotelCheckOut(hotelDTO.getHotelCheckOut());
            hotelRepository.save(hotel);  // 이름 저장

            // 이미지 수정 처리
            if (newImageFiles != null && !newImageFiles.isEmpty()) {
                // 이미지를 여러 개 다룰 경우, 기존 이미지 삭제 및 새 이미지 업로드
                List<Image> existingImages = hotel.getHotelImageList();  // 연결된 이미지들 가져오기

                // 기존 이미지 삭제 처리
                for (Image existingImage : existingImages) {
                    fileUpload.FileDelete(IMG_LOCATION, existingImage.getImageName());
                    imageRepository.delete(existingImage);  // 기존 이미지 삭제
                }

                // 이미지 리스트에서 삭제된 이미지를 제거
                hotel.getHotelImageList().clear();  // 이미지를 모두 제거

                // 새 이미지들 업로드 처리
                List<String> newFilenames = fileUpload.FileUpload(IMG_LOCATION, newImageFiles);

                // 업로드된 새 이미지들 저장
                for (int i = 0; i < newFilenames.size(); i++) {
                    Image newImage = new Image();
                    newImage.setImageName(newFilenames.get(i));
                    newImage.setImageOriginalName(newImageFiles.get(i).getOriginalFilename());
                    newImage.setImagePath(IMG_LOCATION + newFilenames.get(i));

                    // 이미지와 회사 관계 설정
                    newImage.setHotel(hotel);  // 이미지와 회사 연결

                    // 이미지 저장
                    imageRepository.save(newImage);

                    // 이미지 연결 (회사 정보에 이미지 추가)
                    hotel.getHotelImageList().add(newImage);  // 이미지를 회사의 이미지 리스트에 추가
                }

                // 정보 저장 (이미지와의 관계 업데이트)
                hotelRepository.save(hotel);  // 호텔 정보와 연결된 이미지를 저장
            }
        } else {
            throw new RuntimeException("호텔을 찾을 수 없습니다.");
        }
    }
    // 삭제
    @Transactional
    public void delete(Integer hotelId) {
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();

            // 연결된 이미지 삭제
            List<Image> imagesToDelete = hotel.getHotelImageList();
            for (Image image : imagesToDelete) {
                // 이미지 서비스에서 물리적 파일 삭제 + DB에서 삭제
                imageService.deleteImage(image.getImageId());
            }

            // 삭제
            hotelRepository.delete(hotel);
        } else {
            throw new RuntimeException("호텔을 찾을 수 없습니다.");
        }
    }


}
