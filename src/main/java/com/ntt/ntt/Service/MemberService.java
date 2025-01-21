package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 로그인시 입력한 이메일이 존재하는지 조회
        Optional<Member> member = memberRepository.findByMemberEmail(email);

        // 입력한 이메일이 존재하면 로그인
        if (member.isPresent()) {
            Member memberEntity = member.get();

            // 회원의 상태가 '비활성'인 경우 로그인 차단
            if ("비활성".equals(memberEntity.getMemberStatus())) {
                throw new UsernameNotFoundException("이메일이 비활성화된 계정입니다: " + email);
            }
            log.info("입력한 이메일이 존재합니다.");
            return User.withUsername(member.get().getMemberEmail())
                    .password(member.get().getMemberPassword()) // 암호화된 비밀번호 반환
                    .roles(member.get().getRole().name())
                    .build();
        } else { // 입력한 이메일이 존재하지 않으면 로그인 실패
            throw new UsernameNotFoundException(email + " 오류!");
        }
    }

    // 일반 유저 회원가입
    public void saveUser(MemberDTO memberDTO){
        // 회원가입시 입력한 이메일이 존재하는지 조회
        Optional<Member> read = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        // 입력한 이메일이 존재하면 회원가입 실패
        if (read.isPresent()) {
            throw new IllegalStateException("이미 가입된 회원입니다");
        }

        if (memberDTO.getMemberPassword() == null) {
            throw new IllegalArgumentException("비밀번호가 null입니다.");
        }

        // 입력한 이메일이 존재하지 않으면 회원가입 성공
        String password = passwordEncoder.encode(memberDTO.getMemberPassword());
        Member member = modelMapper.map(memberDTO, Member.class);
        member.setMemberName(memberDTO.getMemberName());
        member.setMemberPhone(memberDTO.getMemberPhone());
        member.setMemberPassword(password); // 암호화한 비밀번호 저장
        member.setMemberStatus("활성"); // 회원가입시 memberStatus 는 기본적으로 '활성' 상태로 가입
        member.setRole(Role.USER);
        memberRepository.save(member);

    }

    // 관리자 회원가입
    public void saveManager(MemberDTO memberDTO) {
        // 회원가입시 입력한 이메일이 존재하는지 조회
        Optional<Member> read = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        // 입력한 이메일이 존재하면 회원가입 실패
        if (read.isPresent()) {
            throw new IllegalStateException("이미 가입된 회원입니다");
        }

        // 입력한 이메일이 존재하지 않으면 회원가입 성공
        String password = passwordEncoder.encode(memberDTO.getMemberPassword());
        Member member = modelMapper.map(memberDTO, Member.class);
        member.setMemberName(memberDTO.getMemberName());
        member.setMemberPhone(memberDTO.getMemberPhone());
        member.setMemberStatus("활성");
        member.setMemberPassword(password);
        member.setRole(memberDTO.getRole());
        memberRepository.save(member);

    }

    // 회원정보 수정
    public Member update(MemberDTO memberDTO) {
        Optional<Member> optionalUser = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        if (optionalUser.isPresent()) {
            Member member = optionalUser.get();

            // 현재 비밀번호 확인
            if (memberDTO.getCurrentPassword() != null && !passwordEncoder.matches(memberDTO.getCurrentPassword(), member.getMemberPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호 확인 및 업데이트
            if (memberDTO.getNewPassword() != null && !memberDTO.getNewPassword().isEmpty()) {
                if (memberDTO.getNewPassword().length() < 6) {
                    throw new IllegalArgumentException("새 비밀번호는 6글자 이상이어야 합니다.");
                }
                if (!memberDTO.getNewPassword().equals(memberDTO.getNewPasswordCheck())) {
                    throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
                }
                member.setMemberPassword(passwordEncoder.encode(memberDTO.getNewPassword())); // 새 비밀번호로 업데이트
            }

            // 다른 정보 업데이트
            member.setMemberName(memberDTO.getMemberName());
            member.setMemberPhone(memberDTO.getMemberPhone());

            return memberRepository.save(member);
        }

        throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
    }

    // 관리자에 의한 회원정보 수정
    public Member adminUpdate(MemberDTO memberDTO) {
        Member member = memberRepository.findById(memberDTO.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        member.setRole(memberDTO.getRole());
        member.setMemberStatus(memberDTO.getMemberStatus());

        return memberRepository.save(member);
    }

    // 회원정보 조회
    public MemberDTO read(String memberEmail) {
        Optional<Member> user = memberRepository.findByMemberEmail(memberEmail);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("해당 이메일의 회원을 찾을 수 없습니다.");
        }

        return modelMapper.map(user.get(), MemberDTO.class);
    }

    // 회원탈퇴
    public void delete(String memberEmail) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원을 찾을 수 없습니다."));

        memberRepository.delete(member); // 회원 삭제
        log.info("회원 탈퇴 성공: {}", memberEmail);
    }

    // 전체 회원 조회
    public List<MemberDTO> getAllMembers() {
        List<Member> members = memberRepository.findAll(); // 모든 회원을 조회
        System.out.println("전체 회원 수: " + members.size()); // 디버깅용 로그
        return members.stream()
                .map(member -> modelMapper.map(member, MemberDTO.class))
                .collect(Collectors.toList());
    }

    // 회원 검색기능
    public List<MemberDTO> getFilteredMembers(String role, String email, String status, String name, String phone, String startDate, String endDate) {
        List<Member> members = memberRepository.findAll(); // 기본적으로 모든 회원 조회

        // 필터링 로직 추가
        if (role != null && !role.isEmpty()) {
            members = members.stream()
                    .filter(member -> member.getRole().name().equals(role))
                    .collect(Collectors.toList());
        }
        if (email != null && !email.isEmpty()) {
            members = members.stream()
                    .filter(member -> member.getMemberEmail().contains(email))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty() && !"전체".equals(status)) {
            members = members.stream()
                    .filter(member -> member.getMemberStatus().equals(status))
                    .collect(Collectors.toList());
        }
        if (name != null && !name.isEmpty()) {
            members = members.stream()
                    .filter(member -> member.getMemberName().contains(name))
                    .collect(Collectors.toList());
        }
        if (phone != null && !phone.isEmpty()) {
            members = members.stream()
                    .filter(member -> member.getMemberPhone().contains(phone))
                    .collect(Collectors.toList());
        }

        // 가입일 범위 필터링
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            members = members.stream()
                    .filter(member -> {
                        LocalDate regDate = member.getRegDate().toLocalDate();
                        return (regDate.isEqual(start) || regDate.isAfter(start)) && (regDate.isEqual(end) || regDate.isBefore(end));
                    })
                    .collect(Collectors.toList());
        }

        return members.stream()
                .map(member -> modelMapper.map(member, MemberDTO.class))
                .collect(Collectors.toList());
    }

}
