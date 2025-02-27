package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ImageDTO {

    private Integer imageId;

    private String imageName;

    private String imageOriginalName;

    private String imagePath;

    private String imageMain;

    private Company companyId;

    private Hotel hotelId;

    private Room roomId;

    private ServiceCate serviceCateId;

    private ServiceMenu serviceMenuId;

    private Notice noticeId;

    private Banner bannerId;

    public static ImageDTO fromEntity(Image image) {
        return new ImageDTO(
                image.getImageId(),
                image.getImageName(),
                image.getImageOriginalName(),
                image.getImagePath(),
                image.getImageMain(),
                image.getCompany(),
                image.getHotel(),
                image.getRoom(),
                image.getServiceCate(),
                image.getServiceMenu(),
                image.getNotice(),
                image.getBanner()
        );
    }

}
