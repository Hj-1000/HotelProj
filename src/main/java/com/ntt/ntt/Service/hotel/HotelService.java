package com.ntt.ntt.Service.hotel;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Repository.company.CompanyRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Util.FileUpload;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

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
    public void register(HotelDTO hotelDTO, List<MultipartFile> imageFiles, String memberEmail) {

        /* Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("본사가 존재하지 않습니다.")); */
        // 로그인한 회원 정보 조회
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        Hotel hotel = modelMapper.map(hotelDTO, Hotel.class);

        // 이메일 로그인된 회원의 이메일로 설정
        hotel.setHotelEmail(member.getMemberEmail());

        // 🔹 회원 정보 설정 -> memberId 추가를 위해
        hotel.setMember(member);

        // 1. Hotel 먼저 저장
        hotelRepository.save(hotel);

        // 2. imageFiles를 ImageService를 통해 저장
        imageService.registerHotelImage(hotel.getHotelId(), imageFiles);

        hotelRepository.flush(); // ✅ 즉시 DB 반영
    }



    //본사관리자용목록
    public Page<HotelDTO> listByCompany(Pageable page, String keyword, Integer keyword1, String searchType, Integer companyId) {
        // 1. 페이지 정보 재가공
        int currentPage = page.getPageNumber(); // 기존 페이지 번호 그대로 사용
        int pageSize = page.getPageSize(); // 페이지 사이즈 그대로 사용
        Pageable pageable = PageRequest.of(
                currentPage, pageSize,
                Sort.by(Sort.Direction.ASC, "hotelId") // 최신순으로 정렬
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
        }

        // 3. Hotel -> HotelDTO 변환
        // Hotel -> HotelDTO 변환
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

    //사용자용 호텔 목록
    @Transactional(readOnly = true)
    public Page<HotelDTO> list(Pageable page, String keyword, String searchType, boolean exactMatch) {
        int currentPage = page.getPageNumber();
        int pageSize = 9;
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "hotelId"));

        Page<Hotel> hotels = null;

        if (keyword != null && !keyword.isEmpty()) {
            if ("name".equals(searchType)) {
                hotels = hotelRepository.findByHotelNameLike("%" + keyword + "%", pageable);
            } else if ("location".equals(searchType)) {
                if (exactMatch) {
                    hotels = hotelRepository.findByHotelLocationEquals(keyword, pageable);
                } else {
                    hotels = hotelRepository.findByHotelLocationLike("%" + keyword + "%", pageable);
                }
            } else if ("address".equals(searchType)) {
                hotels = hotelRepository.findByHotelAddressLike("%" + keyword + "%", pageable);
            } else if ("rating".equals(searchType)) {
                hotels = hotelRepository.findByHotelRating(Integer.parseInt(keyword), pageable);
            }
        } else {
            hotels = hotelRepository.findAll(pageable);
        }

        // Hotel -> HotelDTO 변환
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

            // 가장 저렴한 roomPrice 찾기
            Integer cheapestRoomPrice = entity.getRooms().stream()
                    .mapToInt(Room::getRoomPrice)  // roomPrice를 int로 추출
                    .min()  // 최솟값 찾기
                    .orElse(0); // 방이 없다면 기본값 0

            hotelDTO.setCheapestRoomPrice(cheapestRoomPrice); // 가장 저렴한 가격 설정

            return hotelDTO;
        });

        return hotelDTOS;
    }


    // 추천 호텔 목록 가져오기
    public List<HotelDTO> listRecommendedHotels() {
        Pageable pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "hotelId"));
//        Page<Hotel> hotelPage = hotelRepository.findByHotelRating(true, pageable);
        Page<Hotel> hotelPage = hotelRepository.findAll(pageable);

        List<HotelDTO> hotelDTOS = hotelPage.stream()
                .map(hotel -> {
                    HotelDTO hotelDTO = modelMapper.map(hotel, HotelDTO.class);

                    List<ImageDTO> imagesDTOList = imageRepository.findByHotel_HotelId(hotel.getHotelId())
                            .stream()
                            .map(image -> {
                                image.setImagePath(image.getImagePath().replace("c:/data/", ""));
                                return modelMapper.map(image, ImageDTO.class);
                            })
                            .collect(Collectors.toList());
                    hotelDTO.setHotelImgDTOList(imagesDTOList);
                    return hotelDTO;
                })
                .collect(Collectors.toList());

        return hotelDTOS;
    }


//    public Page<HotelDTO> list(Pageable page, String keyword, String searchType, boolean exactMatch) {
//
//        int currentPage = page.getPageNumber();  // Page.getPageNumber()는 0부터 시작
//        int pageSize = 9; // 한 페이지에 9개씩 표시
//        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "hotelId"));
//
//        Page<Hotel> hotels = null;
//
//        if (keyword != null && !keyword.isEmpty()) {
//            if ("name".equals(searchType)) {
//                // 호텔명이 포함된 경우
//                hotels = hotelRepository.findByHotelNameLike("%" + keyword + "%", pageable);
//            } else if ("location".equals(searchType)) {
//                // 정확히 일치하는 location 검색
//                if (exactMatch) {
//                    hotels = hotelRepository.findByHotelLocationEquals(keyword, pageable);
//                } else {
//                    // location에서 %가 포함되도록
//                    hotels = hotelRepository.findByHotelLocationLike("%" + keyword + "%", pageable);
//                }
//            } else if ("address".equals(searchType)) {
//                // 주소 검색
//                hotels = hotelRepository.findByHotelAddressLike("%" + keyword + "%", pageable);
//            } else if ("rating".equals(searchType)) {
//                // 별점 검색
//                hotels = hotelRepository.findByHotelRating(Integer.parseInt(keyword), pageable);
//            }
//        } else {
//            // 검색어가 없으면 모든 호텔 리스트를 조회
//            hotels = hotelRepository.findAll(pageable);
//        }
//
//        // Hotel -> HotelDTO 변환
//        Page<HotelDTO> hotelDTOS = hotels.map(entity -> modelMapper.map(entity, HotelDTO.class));
//
//        return hotelDTOS;
//    }


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

    // hotelId에 맞는 방들을 가져오는 메서드
    public Page<RoomDTO> roomListByHotel(Integer hotelId, Pageable page) {

        // 1. 페이지 정보 재가공
        int currentPage = page.getPageNumber(); // 기존 페이지 번호 그대로 사용
        int pageSize = page.getPageSize(); // 페이지 사이즈 그대로 사용
        Pageable pageable = PageRequest.of(
                currentPage, pageSize,
                Sort.by(Sort.Direction.ASC, "hotelId") // 등록순 정렬
        );

        // 2. 검색타입에 따른 호텔 조회
        Page<Room> rooms = null;
        rooms = roomRepository.findByHotelId_HotelId(hotelId, pageable);

        // 3. Hotel -> HotelDTO 변환
        Page<RoomDTO> roomDTOS = rooms.map(entity -> {
            RoomDTO roomDTO = modelMapper.map(entity, RoomDTO.class);

            // 호텔에 대한 이미지 리스트 가져오기
            List<ImageDTO> imgDTOList = imageRepository.findByRoom_RoomId(entity.getRoomId())
                    .stream()
                    .map(imagefile -> {
                        imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", "")); // 경로 수정
                        return modelMapper.map(imagefile, ImageDTO.class);
                    })
                    .collect(Collectors.toList());

            roomDTO.setRoomImageDTOList(imgDTOList); // 이미지 DTO 리스트 설정
            return roomDTO;
        });

        return roomDTOS;
    }

//    public Page<RoomDTO> getRoomsByHotelId(Integer hotelId, Pageable pageable) {
//        // 호텔 ID에 맞는 Room 데이터 조회 (페이징 처리)
//        Page<Room> roomsPage = roomRepository.findByHotelId_HotelId(hotelId, pageable);
//
//        // 각 Room 객체를 RoomDTO로 변환하여 리스트에 추가
//        List<RoomDTO> roomDTOs = roomsPage.getContent().stream()
//                .map(room -> modelMapper.map(room, RoomDTO.class))
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(roomDTOs, pageable, roomsPage.getTotalElements());
//    }



    // 정보 수정 (이미지 추가 포함)
    @Transactional
    public void update(HotelDTO hotelDTO, List<MultipartFile> newImageFiles) {
        // 호텔 조회 및 수정
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelDTO.getHotelId());
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();

            // 호텔 정보 수정
            hotel.setHotelName(hotelDTO.getHotelName());
            hotel.setHotelLocation(hotelDTO.getHotelLocation());
            hotel.setHotelAddress(hotelDTO.getHotelAddress());
            hotel.setHotelEmail(hotelDTO.getHotelEmail());
            hotel.setHotelPhone(hotelDTO.getHotelPhone());
            hotel.setHotelInfo(hotelDTO.getHotelInfo());
            hotel.setHotelCheckIn(hotelDTO.getHotelCheckIn());
            hotel.setHotelCheckOut(hotelDTO.getHotelCheckOut());

            // 호텔 정보 저장
            hotelRepository.save(hotel);

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

                    // 이미지와 호텔 관계 설정
                    newImage.setHotel(hotel);  // 이미지와 호텔 연결
                    imageRepository.save(newImage);  // 이미지 저장

                    // 호텔 객체에 새 이미지 추가
                    hotel.getHotelImageList().add(newImage);
                }

                // 이미지 관계 업데이트 후 호텔 저장
                hotelRepository.save(hotel);
            }
        } else {
            throw new RuntimeException("호텔을 찾을 수 없습니다.");  // 호텔을 찾을 수 없는 경우 예외 처리
        }
    }


    public HotelDTO findById(Integer hotelId) {
        return hotelRepository.findById(hotelId)
                .map(hotel -> new HotelDTO(hotel)) // 엔티티 -> DTO 변환
                .orElse(null);
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

    // 현재 선택된 본사의 지사 목록 가져오기
    public List<HotelDTO> getFilteredHotel(Integer companyId) {
        List<Hotel> hotels = hotelRepository.findByCompany_CompanyId(companyId);

        return hotels.stream()
                .map(hotel -> new HotelDTO(hotel))  // HotelDTO 생성자에서 MemberDTO도 포함
                .collect(Collectors.toList());
    }

    public List<HotelDTO> getFilteredHotelsByMember(Integer companyId, Integer hotelId, String role,
                                                    String email, String status, String name,
                                                    String phone, String startDate, String endDate) {
        List<Hotel> hotels = hotelRepository.findByCompanyId(companyId);

        return hotels.stream()
                .filter(hotel -> hotel.getMember() != null) // 멤버가 있는 호텔만 필터링
                .filter(hotel -> hotelId == null || hotel.getHotelId().equals(hotelId)) // 지사 필터링 추가
                .filter(hotel -> role == null || hotel.getMember().getRole().toString().equals(role))
                .filter(hotel -> email == null || hotel.getMember().getMemberEmail().contains(email))
                .filter(hotel -> status == null || hotel.getMember().getMemberStatus().equals(status))
                .filter(hotel -> name == null || hotel.getMember().getMemberName().contains(name))
                .filter(hotel -> phone == null || hotel.getMember().getMemberPhone().contains(phone))
                .filter(hotel -> {
                    if (startDate == null || endDate == null || startDate.isEmpty() || endDate.isEmpty()) {
                        return true; // 날짜 필터링 안 함
                    }

                    LocalDate start = LocalDate.parse(startDate);
                    LocalDate end = LocalDate.parse(endDate);
                    LocalDate memberRegDate = hotel.getMember().getRegDate().toLocalDate();

                    return (memberRegDate.isEqual(start) || memberRegDate.isAfter(start)) &&
                            (memberRegDate.isEqual(end) || memberRegDate.isBefore(end));
                })
                .map(hotel -> modelMapper.map(hotel, HotelDTO.class))
                .collect(Collectors.toList());
    }

    public void updateHotelMemberId(HotelDTO hotelDTO) {
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelDTO.getHotelId());
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            hotel.setMember(hotelDTO.getMemberId());  // 호텔에 memberId 설정
            hotelRepository.save(hotel);  // 호텔 정보 저장
        } else {
            throw new RuntimeException("지사를 찾을 수 없습니다.");
        }
    }

}
