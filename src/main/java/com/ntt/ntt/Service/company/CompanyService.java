package com.ntt.ntt.Service.company;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Repository.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    //등록
    public void register(CompanyDTO companyDTO) {

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        Company company = modelMapper.map(companyDTO, Company.class);
        companyRepository.save(company);
    }

    //목록
    public Page<CompanyDTO> list(Pageable page) {

        //1. 페이지정보를 재가공
        int currentPage = page.getPageNumber()-1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(
                currentPage, pageSize,
                Sort.by(Sort.Direction.DESC, "companyId")
        );

        //2. 조회
        Page<Company> companies = companyRepository.findAll(pageable);
        //검색 findByCompanyName, findByCompanyManager =>if문으로 분류따라 조회
        //검색 findByCompanyNameOrCompanyManager=>검색어가 둘 중 하나라도 포함되면 조회

        //3. 변환
        Page<CompanyDTO> companyDTOS = companies.map(
                entity -> modelMapper.map(entity, CompanyDTO.class)
        );

        return companyDTOS;

    }


    //개별보기
    @Transactional(readOnly = true)
    public CompanyDTO read(Integer companyId) {

        // modelMapper가 null이 아닌지 확인
        if (modelMapper == null) {
            throw new IllegalStateException("ModelMapper가 초기화되지 않았습니다.");
        }

        //companyId를 통해 글 불러오기
        Optional<Company> company = companyRepository.findById(companyId);
        CompanyDTO companyDTO = modelMapper.map(company, CompanyDTO.class);

        return companyDTO;

    }

    //수정
    public void update(CompanyDTO companyDTO) {
        //해당 데이터의 id로 조회
        Optional<Company> company = companyRepository.findById(companyDTO.getCompanyId());
        if (company.isPresent()) { //존재하면 수정
            //변환
            Company company1 = modelMapper.map(companyDTO, Company.class);
            //저장
            companyRepository.save(company1);
        }
    }


    //삭제
    public void delete(Integer companyId) {
        companyRepository.deleteById(companyId);
    }



}
