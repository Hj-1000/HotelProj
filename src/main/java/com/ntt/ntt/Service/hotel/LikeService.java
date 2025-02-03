package com.ntt.ntt.Service.hotel;


import com.ntt.ntt.DTO.LikeDetailDTO;
import com.ntt.ntt.DTO.LikeHotelDTO;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.LikeHotel;
import com.ntt.ntt.Entity.Likes;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Repository.hotel.LikeHotelRepository;
import com.ntt.ntt.Repository.hotel.LikeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final HotelRepository hotelRepository; //즐겨찾기에 넣을 호텔 을 찾기위해
    private final MemberRepository memberRepository;
                        //즐겨찾기 를 만들 회원
    private final LikeRepository likeRepository; //즐겨찾기  저장, 수정등
    private final LikeHotelRepository likeHotelRepository; //즐겨찾기 에 담을 즐찾호텔


    //등록
    public Integer addLikeHotel(LikeHotelDTO likehotelDTO, String memberEmail) {
        //컨트롤러에서 입력받은 likehotelDTO의 id를 통해 상품을 찾습니다.
        Hotel hotel = hotelRepository.findById(likehotelDTO.getHotelId())
                .orElseThrow(EntityNotFoundException::new);


        // email Like를 찾습니다.
        Optional<Member> member = memberRepository.findByMemberEmail(memberEmail);

        Likes likes = likeRepository.findByMember_MemberId(member.get().getMemberId());
        //회원으로 찾은 Like가 없다면
        if (likes == null) {
            likes = likes.createLike(member.orElse(null));
            likeRepository.save(likes);
        }

        //찾은 상품을 likehotel으로 변환합니다.

        //즐겨찾기에 이미 같은 상품이 있다면을 알고 싶어서 즐겨찾기호텔 을 찾아봅니다.
        LikeHotel savedlikeHotel
                = likeHotelRepository.findByLikes_LikesIdAndHotel_HotelId
                (likes.getLikesId(), hotel.getHotelId());

        //변환된 X , 직접 즐겨찾기 아이디와 호텔 id로 찾거나
        // 직접 생성해서 likehotel을 Like에 담습니다.
        if(savedlikeHotel !=null){
            return savedlikeHotel.getLikeHotelId();
        }else {
            LikeHotel likeHotel
                    =LikeHotel.createLikeHotel(likes, hotel);
            return likeHotel.getLikeHotelId();
        }

    }


    //목록
    @Transactional(readOnly = true)
    public Page<LikeDetailDTO> likeList(String memberEmail, Pageable pageable) {

        Optional<Member> member = memberRepository.findByMemberEmail(memberEmail);

        Likes likes = likeRepository.findByMember_MemberId(member.get().getMemberId());

        if (likes == null) {
            return Page.empty();  // 빈 페이지를 반환
        }

        return likeHotelRepository.likeList(likes.getLikesId(), pageable);
    }



    //현재 즐겨찾기 가 내꺼인지 확인하는 메소드
    @Transactional(readOnly = true)
    public boolean validateLikeHotel(Integer likeHotelId, String email){

        LikeHotel likehotel = likeHotelRepository
                .findById(likeHotelId).orElseThrow(EntityNotFoundException::new);

        if(likehotel.getCreateBy().equals(email)){
            // ==이면 객체를 비교하고 , .equals는 값을 비교한다.
            return true;
        }
        return false;
    }

    //삭제(취소)
    public String deleteLikeHotel(Integer likeHotelId){
        LikeHotel likeHotel
                = likeHotelRepository
                .findById(likeHotelId)
                .orElseThrow(EntityNotFoundException::new);
        String likeHotelName = likeHotel.getHotel().getHotelName();
        likeHotelRepository.delete(likeHotel);

        return likeHotelName;
    }





}
