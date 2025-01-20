package com.ntt.ntt.Service.hotel;

import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Util.FileUpload;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelService {
    //호텔 서비스
    @Value("${uploadPath}") //이미지가 저장될 위치
    private String imgLocation;

    //호텔 레포지토리
    private final HotelRepository hotelRepository;
    //모델매퍼
    private final ModelMapper modelMapper;
    //이미지
    private final FileUpload fileUpload;

    //등록
    public void hotelRegister(HotelDTO hotelDTO) {

        //변환
        Hotel hotel = modelMapper.map(hotelDTO, Hotel.class);
        //이미지 저장
//        String img = fileUpload.FileUpload(imgLocation, hotelImg);
//        hotel.setHotelImg(img);

        hotelRepository.save(hotel);
    }


    //읽기
    public HotelDTO hotelRead(Integer hotelId) {

        Optional<Hotel> hotel = hotelRepository.findById(hotelId);
        HotelDTO hotelDTO = modelMapper.map(hotel, HotelDTO.class);

        return hotelDTO;
    }



    //목록

    //수정

    //삭제


}
