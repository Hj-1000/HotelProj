package com.ntt.ntt.Service.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.*;
import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Repository.RoomReviewRepository;
import com.ntt.ntt.Repository.company.CompanyRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Util.FileUpload;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
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
    private final RoomReviewRepository roomReviewRepository;

    private final ModelMapper modelMapper;

    @Autowired
    private final ImageService imageService;
    @Autowired
    private FileUpload fileUpload;

    //호텔 레포지토리
    private final HotelRepository hotelRepository;


    // 회원 목록 불러오기
    public List<MemberDTO> getAllManagers() {
        List<Member> members = memberRepository.findByRole(Role.MANAGER);
        List<Integer> hotelMembers = hotelRepository.findAll().stream()
                .map(hotel -> hotel.getMember().getMemberId())
                .collect(Collectors.toList());

        return members.stream()
                .filter(member -> !hotelMembers.contains(member.getMemberId()))
                .map(a -> new MemberDTO(a.getMemberId(), a.getMemberEmail(), a.getMemberName()))
                .collect(Collectors.toList());
    }

    // 모든 회사 목록 불러오기
    public List<CompanyDTO> getAllCompany() {
        return companyRepository.findAll().stream()
                .map(company -> new CompanyDTO(company.getCompanyId(), company.getCompanyName()))
                .collect(Collectors.toList());
    }

    // 현재 사용자가 소속된 회사 목록 불러오기
    public List<CompanyDTO> getMyCompany() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memberEmail = authentication.getName();

        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        return companyRepository.findByMember_MemberId(member.getMemberId()).stream()
                .map(company -> new CompanyDTO(company.getCompanyId(), company.getCompanyName()))
                .collect(Collectors.toList());
    }

    // 호텔 등록
    @Transactional
    public void register(HotelDTO hotelDTO, List<MultipartFile> imageFiles, String memberEmail) {
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        Hotel hotel = modelMapper.map(hotelDTO, Hotel.class);
        if (hotelDTO.getCompanyId() != null) {
            Company company = companyRepository.findById(hotelDTO.getCompanyId())
                    .orElseThrow(() -> new IllegalStateException("유효하지 않은 Company ID입니다."));
            hotel.setCompany(company);
        } else {
            throw new IllegalStateException("Company 정보가 제공되지 않았습니다.");
        }

        hotelRepository.save(hotel);
        hotelRepository.flush();

        imageService.registerHotelImage(hotel.getHotelId(), imageFiles);
    }


    //관리자용 호텔목록
    public Page<HotelDTO> listByAdmin(Pageable page, String keyword, Integer keyword1, String searchType, Integer companyId) {
        // 1. 페이지 정보 재가공
        int currentPage = page.getPageNumber();
        int pageSize = page.getPageSize();
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "hotelId"));

        // 2. 검색타입에 따른 호텔 조회
        Page<Hotel> hotels;

        if (companyId != null) { // companyId가 있을 때만 실행
            hotels = searchHotelsByCompanyId(companyId, keyword, keyword1, searchType, pageable);
        } else {
            hotels = searchHotelsWithoutCompanyId(keyword, keyword1, searchType, pageable);
        }

        // 3. Hotel -> HotelDTO 변환
        return hotels.map(entity -> {
            HotelDTO hotelDTO = modelMapper.map(entity, HotelDTO.class);

            // 호텔 이미지 리스트 변환
            List<ImageDTO> imgDTOList = imageRepository.findByHotel_HotelId(entity.getHotelId())
                    .stream()
                    .map(imagefile -> {
                        imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", ""));
                        return modelMapper.map(imagefile, ImageDTO.class);
                    })
                    .collect(Collectors.toList());

            hotelDTO.setHotelImgDTOList(imgDTOList);
            return hotelDTO;
        });
    }


    // CompanyId 포함 목록
    private Page<Hotel> searchHotelsByCompanyId(Integer companyId, String keyword, Integer keyword1, String searchType, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            String keywordLike = "%" + keyword + "%";

            switch (searchType) {
                case "name":
                    return hotelRepository.findByCompany_CompanyIdAndHotelNameLike(companyId, keywordLike, pageable);
                case "location":
                    return hotelRepository.findByCompany_CompanyIdAndHotelLocationLike(companyId, keywordLike, pageable);
                case "address":
                    return hotelRepository.findByCompany_CompanyIdAndHotelAddressLike(companyId, keywordLike, pageable);
                case "rating":
                    return hotelRepository.findByCompany_CompanyIdAndHotelRatingLike(companyId, keyword1, pageable);
            }
        }
        return hotelRepository.findByCompany_CompanyId(companyId, pageable);
    }

    // CompanyId 미포함
    private Page<Hotel> searchHotelsWithoutCompanyId(String keyword, Integer keyword1, String searchType, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            String keywordLike = "%" + keyword + "%";

            switch (searchType) {
                case "company":
                    return hotelRepository.findByCompany_CompanyNameLike(keywordLike, pageable);
                case "name":
                    return hotelRepository.findByHotelNameLike(keywordLike, pageable);
                case "location":
                    return hotelRepository.findByHotelLocationLike(keywordLike, pageable);
                case "address":
                    return hotelRepository.findByHotelAddressLike(keywordLike, pageable);
                case "rating":
                    return hotelRepository.findByHotelRating(keyword1, pageable);
            }
        }
        return hotelRepository.findAll(pageable);
    }


    //호텔장용 목록
    //로그인한 chief의 담당 companyId가 겹치는 hotel 목록
    public Page<HotelDTO> listByChief(Pageable pageable, String keyword, Integer keyword1, String searchType, Integer memberId) {
        // 1. 현재 로그인한 회원의 memberId로 companyId를 찾는다.
        List<Company> companies = companyRepository.findByMember_MemberId(memberId); // 리스트로 반환되므로 수정
        Integer companyId = null;

        if (companies != null && !companies.isEmpty()) {
            companyId = companies.get(0).getCompanyId(); // 첫 번째 회사에서 companyId 가져오기
        }

        if (companyId == null) {
            // 만약 회원이 등록된 회사가 없으면 빈 결과 반환
            return Page.empty(pageable);
        }

        // 2. 검색 조건에 따라 호텔 조회
        Page<Hotel> hotels;
        if (keyword != null && !keyword.isEmpty()) {
            String keywordLike = "%" + keyword + "%";  // LIKE 조건을 위한 검색어 처리

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
                hotels = hotelRepository.findByCompany_CompanyIdAndHotelRatingLike(companyId, keyword1, pageable);
            } else {
                // 검색어가 없으면 companyId에 해당하는 모든 호텔 조회
                hotels = hotelRepository.findByCompany_CompanyId(companyId, pageable);
            }
        } else {
            // 검색어가 없으면 companyId에 해당하는 모든 호텔 조회
            hotels = hotelRepository.findByCompany_CompanyId(companyId, pageable);
        }

        // 3. Hotel -> HotelDTO 변환
        return hotels.map(entity -> {
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
    }


    //호텔 매니저용
    public Page<HotelDTO> listByManager(Integer memberId, Pageable pageable) {
        // 1. memberId에 해당하는 호텔 조회
        // memberId와 연관된 호텔을 조회
        Page<Hotel> hotels = hotelRepository.findByMember_MemberId(memberId, pageable);

        // 2. Hotel -> HotelDTO 변환
        return hotels.map(entity -> {
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
    }


    //일반회원용 호텔 목록
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

            // 천 단위로 콤마 추가한 문자열로 변환
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            String formattedPrice = decimalFormat.format(cheapestRoomPrice);

            // 결과를 hotelDTO에 설정
            hotelDTO.setCheapestRoomPrice(formattedPrice); // String으로 된 가격 설정

            return hotelDTO;
        });

        return hotelDTOS;
    }


    // 추천 호텔 목록 가져오기(별점높은순)
    public List<HotelDTO> listRecommendedHotels() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "hotelRating").and(Sort.by(Sort.Direction.DESC, "hotelId")));

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

                    Integer cheapestRoomPrice = hotel.getRooms().stream()
                            .mapToInt(Room::getRoomPrice)
                            .min()
                            .orElse(0);

                    DecimalFormat decimalFormat = new DecimalFormat("#,###");
                    hotelDTO.setCheapestRoomPrice(decimalFormat.format(cheapestRoomPrice));

                    return hotelDTO;
                })
                .sorted(Comparator
                        .comparing(HotelDTO::getRoomReviewCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(HotelDTO::getHotelRating, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .limit(4)
                .collect(Collectors.toList());

        return hotelDTOS;
    }




    //개별보기
    @Transactional(readOnly = true)
    public HotelDTO read(Integer hotelId) {

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        // hotelId를 통해 호텔 정보 불러오기
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("호텔 정보를 찾을 수 없습니다."));

        // Hotel → HotelDTO 변환
        HotelDTO hotelDTO = modelMapper.map(hotel, HotelDTO.class);

        // 🔹 memberName 추가 (hotel.getMember()가 null이 아닐 경우)
        if (hotel.getMember() != null) {
            hotelDTO.setMemberName(hotel.getMember().getMemberName());
        }

        // 객실 리뷰 개수 계산 (DB에서)
        Integer roomReviewCount = roomReviewRepository.countReviewsByHotelId(hotelId);
        hotelDTO.setRoomReviewCount(roomReviewCount); // 호텔 DTO에 리뷰 개수 설정

        // 이미지 경로에서 c:/data/ 제거
        List<ImageDTO> imgDTOList = imageRepository.findByHotel_HotelId(hotelDTO.getHotelId())
                .stream()
                .map(imagefile -> {
                    // 이미지 경로를 상대 경로로 변환
                    imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", ""));
                    return modelMapper.map(imagefile, ImageDTO.class);
                })
                .collect(Collectors.toList());

        hotelDTO.setHotelImgDTOList(imgDTOList);

        return hotelDTO;
    }

    // hotelId에 맞는 방들을 가져오는 메서드
    public Page<RoomDTO> roomListByHotel(Integer hotelId, Pageable page) {

        // 1. 페이지 정보 그대로 사용
        Pageable pageable = PageRequest.of(page.getPageNumber(), 10, Sort.by(Sort.Direction.ASC, "hotelId"));

        // 2. 호텔 데이터 페이징 처리
        Page<Room> rooms = roomRepository.findByHotelId_HotelId(hotelId, pageable);

        // 3. Room -> RoomDTO 변환
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

    //hotelId에 맞는 방을 가져오는 메서드2(사용자용)
    public List<RoomDTO> getRoomsByHotel(Integer hotelId) {
        // 페이징 없이 모든 방 데이터를 가져오기
        List<Room> rooms = roomRepository.findByHotelId_HotelId(hotelId);

        // Room -> RoomDTO 변환 후 반환
        return rooms.stream().map(entity -> {
            RoomDTO roomDTO = modelMapper.map(entity, RoomDTO.class);

            // 이미지 처리, null 방지 등 추가 처리
            if (roomDTO.getRoomImageDTOList() == null) {
                roomDTO.setRoomImageDTOList(new ArrayList<>()); // null 방지
            }

            List<ImageDTO> imgDTOList = Optional.ofNullable(imageRepository.findByRoom_RoomId(entity.getRoomId()))
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(imagefile -> {
                        if (imagefile.getImagePath() != null) {
                            imagefile.setImagePath(imagefile.getImagePath().replace("c:/data/", ""));
                        }
                        return modelMapper.map(imagefile, ImageDTO.class);
                    })
                    .collect(Collectors.toList());

            roomDTO.setRoomImageDTOList(imgDTOList); // 이미지 DTO 리스트 설정

            return roomDTO;
        }).collect(Collectors.toList());
    }






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
                    if ((startDate == null || startDate.isEmpty()) && (endDate == null || endDate.isEmpty())) {
                        return true; // 날짜 필터링 안 함
                    }

                    LocalDate memberRegDate = hotel.getMember().getRegDate().toLocalDate();

                    if (startDate != null && !startDate.isEmpty()) {
                        LocalDate start = LocalDate.parse(startDate);
                        if (memberRegDate.isBefore(start)) {
                            return false; // 시작 날짜 이후만 허용
                        }
                    }
                    if (endDate != null && !endDate.isEmpty()) {
                        LocalDate end = LocalDate.parse(endDate);
                        if (memberRegDate.isAfter(end)) {
                            return false; // 종료 날짜 이전만 허용
                        }
                    }

                    return true; // 조건 만족
                })
                .map(hotel -> modelMapper.map(hotel, HotelDTO.class))
                .collect(Collectors.toList());
    }

    public void updateHotelMemberId(HotelDTO hotelDTO) {
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelDTO.getHotelId());
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();

            // memberId를 설정
            hotel.setMember(hotelDTO.getMemberId()); // 기존 memberId로 hotel의 member 설정

            // member 객체에서 memberEmail을 가져와 hotelEmail에 설정
            Member member = hotelDTO.getMemberId(); // 여기서 memberId가 Member 객체로 설정되어 있다고 가정
            if (member != null) {
                hotel.setHotelEmail(member.getMemberEmail());  // hotelEmail을 memberEmail로 설정
            } else {
                throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
            }

            // 호텔 정보 저장
            hotelRepository.save(hotel);
        } else {
            throw new RuntimeException("지사를 찾을 수 없습니다.");
        }
    }



}
