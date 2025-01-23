package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.ServiceCate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCateRepository extends JpaRepository<ServiceCate, Integer> {
    public Page<ServiceCate> findByServiceCateNameLike(String keword, Pageable pageable);
}
