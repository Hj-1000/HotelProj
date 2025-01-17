package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberSevice implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 로그인시 입력한 이메일이 존재하는지 조회
        Optional<Member> member = memberRepository.findByMemberEmail(email);

        // 입력한 이메일이 존재하면 로그인 성공
        if (member.isPresent()) {

            return User.withUsername(member.get().getMemberEmail())
                    .password(member.get().getMemberPassword())
                    .roles(member.get().getRole().name())
                    .build();
        } else { // 입력한 이메일이 존재하지 않으면 로그인 실패
            throw new UsernameNotFoundException(email + "오류!");
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
        member.setMemberPassword(password);
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
        member.setMemberPassword(password);
        member.setRole(Role.USER);
        memberRepository.save(member);

    }
}
