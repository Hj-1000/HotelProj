package com.ntt.ntt.Service.hotel;


import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ImageDTO;
import com.ntt.ntt.DTO.LikeDTO;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.LikeHotel;
import com.ntt.ntt.Entity.Likes;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.ImageRepository;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Repository.hotel.LikeHotelRepository;
import com.ntt.ntt.Repository.hotel.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    //등록
    public Integer likeRegister(LikeDTO likeDTO, String memberEmail) {
        System.out.println("찜 요청 받은 이메일: " + likeDTO.getMemberEmail());

        // 회원 조회
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 회원이 존재하지 않습니다."));

        // 기존 Likes 조회 (없으면 새로 생성)
        Likes like = likeRepository.findByMember_MemberEmail(memberEmail);
        if (like == null) {
            like = Likes.builder()
                    .member(member)
                    .build();
            likeRepository.save(like);
        }

        // 호텔 조회 (Optional 처리)
        Hotel hotel = hotelRepository.findById(likeDTO.getHotelId())
                .orElseThrow(() -> new NoSuchElementException("해당 호텔이 존재하지 않습니다."));

        // LikeHotel 매핑
        LikeHotel likehotel = modelMapper.map(likeDTO, LikeHotel.class);
        likehotel.setLikes(like);
        likehotel.setHotel(hotel);
        likehotel.setCreateBy(memberEmail);  // createBy에 memberEmail 설정

        // 기존 찜 데이터가 존재하면 ID를 유지하여 업데이트
        likeHotelRepository.findByHotel_HotelId(likeDTO.getHotelId())
                .stream().findFirst()
                .ifPresent(existingLikeHotel -> likehotel.setLikeHotelId(existingLikeHotel.getLikeHotelId()));

        // 저장
        likeHotelRepository.save(likehotel);

        return likehotel.getLikeHotelId();
    }



    //목록
    public List<LikeDTO> likeList(String email) {
        try {
            List<LikeHotel> likeHotelList = likeHotelRepository.findByLikes_Member_MemberEmail(email);

            if (likeHotelList == null || likeHotelList.isEmpty()) {
                System.out.println("조회된 likeHotelList가 없습니다.");
                return Collections.emptyList();
            }

            List<LikeDTO> likeDTOS = likeHotelList.stream().map(likehotel -> {
                        try {
                            if (likehotel.getHotel() == null) {
                                System.out.println("likehotel의 getHotel()이 null입니다: " + likehotel);
                                return null; // null 값 처리 (필요하면 Optional 사용 가능)
                            }

                            HotelDTO hotelDTO = modelMapper.map(likehotel.getHotel(), HotelDTO.class);

                            LikeDTO likeDTO = LikeDTO.builder()
                                    .likeHotelId(likehotel.getLikeHotelId())
                                    .hotelDTO(hotelDTO)
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

                            return likeDTO;
                        } catch (Exception e) {
                            System.out.println("예외 발생 (LikeHotel -> LikeDTO 변환 중): " + e.getMessage());
                            e.printStackTrace();
                            return null; // 예외 발생 시 해당 요소를 무시하고 계속 진행
                        }
                    }).filter(Objects::nonNull) // null 값 제거
                    .collect(Collectors.toList());

            System.out.println("최종 LikeDTO 리스트: " + likeDTOS);
            return likeDTOS;
        } catch (Exception e) {
            System.out.println("likeList() 메서드 실행 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }



//    public List<LikeDTO> likeList(String email) {
//
//        List<LikeHotel> likeHotelList = likeHotelRepository.findByLikes_Member_MemberEmail(email);
//        System.out.println("조회된 LikeHotel 목록: " + likeHotelList);
//
////        List<hotelDTO> hotelDTOS = null;
////        likeHotelList.forEach(likeHotel -> {
////            hotelDTO hotelDTO = modelMapper.map(likeHotel.getHotel(),hotelDTO.class);
////            hotelDTOS.add(hotelDTO);
////        });
//
//        List<LikeDTO> likeDTOS = likeHotelList.stream().map(likehotel -> {
//            HotelDTO hotelDTO = modelMapper.map(likehotel.getHotel(), HotelDTO.class);
//
//            if (likehotel.getHotel() == null) {
//                System.out.println("likehotel의 getHotel()이 null입니다: " + likehotel);
//                return null;
//            }
//
//            LikeDTO likeDTO = LikeDTO.builder()
//                    .likeHotelId(likehotel.getLikeHotelId())
//                    .hotelDTO(hotelDTO)
//                    .build();
//
//            List<ImageDTO> hotelImageDTOList = imageRepository.findByHotel_HotelId(likehotel.getHotel().getHotelId())
//                    .stream()
//                    .map(hotelImg -> modelMapper.map(hotelImg, ImageDTO.class))
//                    .collect(Collectors.toList());
//
//            System.out.println("호텔 이미지 리스트: " + hotelImageDTOList);
//
//            if (likeDTO.getHotelDTO() != null) {
//                likeDTO.getHotelDTO().setHotelImgDTOList(hotelImageDTOList);
//            } else {
//                System.out.println("likeDTO의 getHotelDTO()가 null입니다!");
//            }
//
//
//            return likeDTO;
//
//        }).collect(Collectors.toList());
//
//        System.out.println(likeDTOS );
//
//        return likeDTOS;
//    }

    //삭제
    @Transactional
    public void likeDelete(int likeHotelId) {
        likeHotelRepository.deleteById(likeHotelId);
    }





}
