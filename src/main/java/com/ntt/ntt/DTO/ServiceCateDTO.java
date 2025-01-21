package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Image;
import lombok.*;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCateDTO {

    private Integer serviceCateId;

    private Image image;

    private String serviceCateName;

    private Hotel hotelId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> serviceCateImageDTOList = new ArrayList<>();




//    public ServiceCateDTO setImageDTOList(List<Image> imageList) {
//        ModelMapper modelMapper = new ModelMapper();
//
//        List<ImageDTO> imageDTOS =
//                imageList.stream().map(image -> modelMapper.map(image, ImageDTO.class)).collect(Collectors.toList());
//
//        this.imageDTOList = imageDTOS;
//
//        return this;

//    }


}
