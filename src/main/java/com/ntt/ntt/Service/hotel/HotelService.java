package com.ntt.ntt.Service.hotel;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Repository.ImageRepository;
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

    private final ModelMapper modelMapper;

    @Autowired
    private final ImageService imageService;
    @Autowired
    private FileUpload fileUpload;

    //호텔 레포지토리
    private final HotelRepository hotelRepository;


    //등록
    public void register(HotelDTO hotelDTO, List<MultipartFile> imageFiles) {

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        Hotel hotel = modelMapper.map(hotelDTO, Hotel.class);

        // 1. Hotel 먼저 저장
        hotelRepository.save(hotel);

        // 2. imageFiles를 ImageService를 통해 저장
        imageService.registerHotelImage(hotel.getHotelId(), imageFiles);
    }


    //목록
    public Page<HotelDTO> list(Pageable page, String keyword, Integer keyword1, String searchType) {

        // 1. 페이지 정보 재가공
        int currentPage = page.getPageNumber() - 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(
                currentPage, pageSize,
                Sort.by(Sort.Direction.DESC, "hotelId")
        );

        // 2. 검색타입에 따른 회사 조회
        Page<Hotel> hotels;

        if (keyword != null && !keyword.isEmpty()) {
            String keywordLike = "%" + keyword + "%";  // LIKE 조건을 위한 검색어 처리

            if ("name".equals(searchType)) {
                // 호텔명이 포함 된 경우
                hotels = hotelRepository.findByHotelNameLike(keywordLike, pageable);
            } else if ("location".equals(searchType)) {
                // 지역이 포함 된 경우
                hotels = hotelRepository.findByHotelLocationLike(keywordLike, pageable);
            } else if ("address".equals(searchType)) {
                // 주소가 포함 된 경우
                hotels = hotelRepository.findByHotelAddressLike(keywordLike, pageable);
            } else if ("rating".equals(searchType)){
                // 별점 검색
                hotels = hotelRepository.findByHotelRating(keyword1, pageable);
            }
        } else {
            // 검색어가 없으면 모든 회사 리스트를 조회
            hotels = hotelRepository.findAll(pageable);
        }

        // 3. Hotel -> HotelDTO 변환
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

    //수정

    //삭제


}
