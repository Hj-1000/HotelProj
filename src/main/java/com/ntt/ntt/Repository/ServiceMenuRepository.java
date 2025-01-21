package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.ServiceMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceMenuRepository extends JpaRepository<ServiceMenu, Integer> {
}
