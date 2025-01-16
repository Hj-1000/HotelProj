package com.ntt.ntt.Service;

import com.ntt.ntt.Repository.ServiceCateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ServiceCateService{
    private final ServiceCateRepository serviceCateRepository;
    private final ModelMapper modelMapper;

    //서비스 카테로고리 등록
    //서비스 카테로고리 읽기
    //서비스 카테로고리 수정
    //서비스 카테로고리 삭제


}
