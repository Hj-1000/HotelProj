package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.*;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ImageDTO {

    private Integer imageId;

    private String imageName;

    private String imagePath;

    private Hotel hotelId;

    private Room roomId;

    private ServiceCate serviceCateId;

    private ServiceMenu serviceMenuId;

    private Notice noticeId;

    private Banner baannerId;

}
