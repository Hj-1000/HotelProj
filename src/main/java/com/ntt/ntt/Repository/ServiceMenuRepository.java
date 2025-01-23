package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Image;
import com.ntt.ntt.Entity.ServiceMenu;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceMenuRepository extends JpaRepository<ServiceMenu, Integer> {
    //카테고리 상관없이 가져오기
    //메뉴 이름으로 검색하기
    public Page<ServiceMenu> findByServiceMenuNameLike(String keyword, Pageable pageable);
    //메뉴 상태로 검색하기
    public Page<ServiceMenu> findByServiceMenuStatusLike(String keyword, Pageable pageable);

    //카테고리에 속한 메뉴 개수 카운트 하는 메서드
    Integer countByServiceCate_ServiceCateId(Integer serviceCateId);
    // 특정 카테고리에 속한 메뉴 조회
    public Page<ServiceMenu> findByServiceCate_ServiceCateId(Integer serviceCateId, Pageable pageable);
    //특정 카테고리에 속한 메뉴 조회(이름 포함)
    public Page<ServiceMenu> findByServiceCate_ServiceCateIdAndServiceMenuNameLike(Integer serviceCateId,String keyword, Pageable pageable);

    public Page<ServiceMenu> findByServiceMenuIdAndServiceCate_ServiceCateName(Integer serviceCateId,String keyword, Pageable pageable);

}
