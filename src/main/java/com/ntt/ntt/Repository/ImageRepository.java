package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    public List<Image> findByCompany_CompanyId(Integer companyId);
    public Image findByCompany_CompanyIdAndImageMain(Integer companyId, String val);

    public List<Image> findByHotel_HotelId(Integer hotelId);
    public Image findByHotel_HotelIdAndImageMain(Integer hotelId, String val);

    public List<Image> findByRoom_RoomId(Integer roomId);
    public Image findByRoom_RoomIdAndImageMain(Integer roomId, String val);

    public List<Image> findByServiceCate_ServiceCateId(Integer serviceCateId);
    public Image findByServiceCate_ServiceCateIdAndImageMain(Integer serviceCateId, String val);

    public List<Image> findByServiceMenu_ServiceMenuId(Integer serviceMenuId);
    public Image findByServiceMenu_ServiceMenuIdAndImageMain(Integer serviceMenuId, String val);

    public List<Image> findByNotice_NoticeId(Integer noticeId);
    public Image findByNotice_NoticeIdAndImageMain(Integer noticeId, String val);

    public List<Image> findByBanner_BannerId(Integer bannerId);
    public Image findByBanner_BannerIdAndImageMain(Integer bannerId, String val);

}
