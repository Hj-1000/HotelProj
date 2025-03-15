package com.ntt.ntt.Service.hotel;


import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.LikeDTO;
import com.ntt.ntt.Entity.*;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Repository.hotel.LikeHotelRepository;
import com.ntt.ntt.Repository.hotel.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final HotelRepository hotelRepository; // 즐겨찾기에 넣을 호텔 을 찾기위해
    private final MemberRepository memberRepository; // 즐겨찾기 를 만들 회원
    private final LikeRepository likeRepository; // 즐겨찾기  저장, 수정등
    private final LikeHotelRepository likeHotelRepository; // 즐겨찾기 에 담을 즐찾호텔
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;


    //이미 즐겨찾기가 되어있는지 확인하고 등록 메소드
    public String likeRegister(LikeDTO likeDTO, String memberEmail) {
        System.out.println("찜 요청 받은 이메일: " + likeDTO.getMemberEmail());

        // 1. 회원 조회
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 회원이 존재하지 않습니다."));

        // 2. 기존 Likes 조회 (없으면 새로 생성)
        Likes likes = likeRepository.findByMember_MemberEmail(memberEmail);
        if (likes == null) {
            likes = Likes.builder()
                    .member(member)
                    .build();
            likeRepository.save(likes);
        }

        // 3. 호텔 조회
        Hotel hotel = hotelRepository.findById(likeDTO.getHotelId())
                .orElseThrow(() -> new NoSuchElementException("해당 호텔이 존재하지 않습니다."));

        // 4. 해당 회원의 장바구니에 이미 같은 호텔이 있는지 확인
        Optional<LikeHotel> existingLikeHotel = likeHotelRepository.findByLikesAndHotel(likes, hotel);
        if (existingLikeHotel.isPresent()) {
            return "이미 스크랩되어 있습니다.";  // 예외를 던지지 않고 메시지 반환
        }

        // 5. 새로운 LikeHotel 매핑
        LikeHotel likeHotel = LikeHotel.builder()
                .likes(likes)
                .hotel(hotel)
                .createdBy(memberEmail)
                .build();

        // 6. 저장 후 메시지 반환
        likeHotelRepository.save(likeHotel);
        return "해당 호텔이 스크랩 되었습니다.";
    }

    //즐겨찾기 체크
    @Transactional
    public void deleteByHotelIdAndMemberId(Integer hotelId, Integer memberId) {
        LikeHotel likeHotel = likeHotelRepository.findByHotel_HotelIdAndLikes_Member_MemberId(hotelId, memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 호텔이 찜 목록에 없습니다."));

        likeHotelRepository.deleteByHotel_HotelIdAndMember_MemberId(hotelId, memberId);
    }

    //목록
    public Page<LikeDTO> likeList(String email, Pageable pageable) {
        try {
            // 좋아요 목록을 페이징하여 조회
            Page<LikeHotel> likeHotelPage = likeHotelRepository.findByLikes_Member_MemberEmail(email, pageable);

            if (likeHotelPage == null || likeHotelPage.isEmpty()) {
                System.out.println("조회된 likeHotelPage가 없습니다.");
                return Page.empty(pageable);  // 빈 페이지 반환
            }

            List<LikeDTO> likeDTOS = likeHotelPage.getContent().stream().map(likehotel -> {
                        try {
                            if (likehotel.getHotel() == null) {
                                System.out.println("likehotel의 getHotel()이 null입니다: " + likehotel);
                                return null; // null 값 처리
                            }

                            HotelDTO hotelDTO = modelMapper.map(likehotel.getHotel(), HotelDTO.class);

                            LikeDTO likeDTO = LikeDTO.builder()
                                    .likeHotelId(likehotel.getLikeHotelId())
                                    .hotelDTO(hotelDTO)
                                    .regDate(likehotel.getRegDate())  // regDate 필드 추가
                                    .build();

                            List<ImageDTO> hotelImageDTOList = imageRepository.findByHotel_HotelId(likehotel.getHotel().getHotelId())
                                    .stream()
                                    .map(hotelImg -> modelMapper.map(hotelImg, ImageDTO.class))
                                    .collect(Collectors.toList());

                            if (likeDTO.getHotelDTO() != null) {
                                likeDTO.getHotelDTO().setHotelImgDTOList(hotelImageDTOList);
                            } else {
                                System.out.println("likeDTO의 getHotelDTO()가 null입니다!");
                            }

                            // 가장 저렴한 roomPrice 찾기
                            Integer cheapestRoomPrice = likehotel.getHotel().getRooms().stream()
                                    .mapToInt(Room::getRoomPrice)  // roomPrice를 int로 추출
                                    .min()  // 최솟값 찾기
                                    .orElse(0); // 방이 없다면 기본값 0

                            // 천 단위로 콤마 추가한 문자열로 변환
                            DecimalFormat decimalFormat = new DecimalFormat("#,###");
                            String formattedPrice = decimalFormat.format(cheapestRoomPrice);

                            // 결과를 hotelDTO에 설정
                            hotelDTO.setCheapestRoomPrice(formattedPrice); // String으로 된 가격 설정

                            return likeDTO;

                        } catch (Exception e) {
                            System.out.println("예외 발생 (LikeHotel -> LikeDTO 변환 중): " + e.getMessage());
                            e.printStackTrace();
                            return null; // 예외 발생 시 해당 요소를 무시하고 계속 진행
                        }
                    }).filter(Objects::nonNull) // null 값 제거
                    .collect(Collectors.toList());

            // regDate 기준 내림차순 정렬
            likeDTOS.sort(Comparator.comparing(LikeDTO::getRegDate).reversed());

            // 페이징 처리된 결과 반환
            return new PageImpl<>(likeDTOS, pageable, likeHotelPage.getTotalElements());  // getTotalElements() 사용

        } catch (Exception e) {
            System.out.println("likeList() 메서드 실행 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable); // 예외 발생 시 빈 페이지 반환
        }
    }


    //즐겨찾기만 삭제
    @Transactional
    public void delete(Integer likeHotelId) {
        //이젠 좀 돼라
        likeHotelRepository.deleteById(likeHotelId);
    }






}
