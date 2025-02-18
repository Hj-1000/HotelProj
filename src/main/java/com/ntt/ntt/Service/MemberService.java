package com.ntt.ntt.Service;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.NotificationRepository;
import com.ntt.ntt.Repository.QnaRepository;
import com.ntt.ntt.Repository.ReplyRepository;
import com.ntt.ntt.Repository.company.CompanyRepository;
import com.ntt.ntt.Repository.hotel.HotelRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final QnaRepository qnaRepository;
    private final ReplyRepository replyRepository;
    private final NotificationRepository notificationRepository;
    private final HotelRepository hotelRepository;
    private final CompanyRepository companyRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 로그인시 입력한 이메일이 존재하는지 조회
        Optional<Member> member = memberRepository.findByMemberEmail(email);

        // 입력한 이메일이 존재하면 로그인
        if (member.isPresent()) {
            Member memberEntity = member.get();

            // 회원의 상태가 '비활성'인 경우 로그인 차단
            if ("비활성".equals(memberEntity.getMemberStatus())) {
                throw new UsernameNotFoundException("이 이메일은 비활성화된 계정입니다.%%%관리자에게 문의해주세요.");
            }
            return User.withUsername(member.get().getMemberEmail())
                    .password(member.get().getMemberPassword()) // 암호화된 비밀번호 반환
                    .roles(member.get().getRole().name())
                    .build();
        } else { // 입력한 이메일이 존재하지 않으면 로그인 실패
            throw new UsernameNotFoundException("로그인에 실패했습니다.%%%이메일과 비밀번호를 확인해주세요.");
        }
    }

    // 일반 유저 회원가입
    public void saveUser(MemberDTO memberDTO) {
        // 회원가입시 입력한 이메일이 존재하는지 조회
        Optional<Member> read = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        // 입력한 이메일이 이미 존재하면 회원가입 실패
        if (read.isPresent()) {
            throw new IllegalStateException("이미 가입된 회원입니다");
        }

        // 입력한 이메일이 존재하지 않으면 회원가입 성공
        String password = passwordEncoder.encode(memberDTO.getMemberPassword());
        Member member = modelMapper.map(memberDTO, Member.class);
        member.setMemberName(memberDTO.getMemberName());
        member.setMemberPhone(memberDTO.getMemberPhone());
        member.setMemberPassword(password); // 암호화된 비밀번호 저장
        member.setMemberStatus("활성"); // 회원가입시 memberStatus 는 기본적으로 '활성' 상태로 가입
        member.setRole(Role.USER); // 일반회원은 회원가입시 USER 권한으로 가입
        memberRepository.save(member);
    }

    // 관리자 회원가입
    public void saveAdmin(MemberDTO memberDTO) {
        // 회원가입시 입력한 이메일이 존재하는지 조회
        Optional<Member> read = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        // 입력한 이메일이 이미 존재하면 회원가입 실패
        if (read.isPresent()) {
            throw new IllegalStateException("이미 가입된 회원입니다");
        }

        // 입력한 이메일이 존재하지 않으면 회원가입 성공
        String password = passwordEncoder.encode(memberDTO.getMemberPassword());
        Member member = modelMapper.map(memberDTO, Member.class);
        member.setMemberName(memberDTO.getMemberName());
        member.setMemberPhone(memberDTO.getMemberPhone());
        member.setMemberPassword(password);
        member.setMemberStatus("활성");
        member.setRole(memberDTO.getRole()); // 관리자는 회원가입시 선택한 권한으로 가입
        memberRepository.save(member);
    }

    // 매니저 회원가입
    public Member saveManager(MemberDTO memberDTO) {
        // 회원가입시 입력한 이메일이 존재하는지 조회
        Optional<Member> read = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        // 입력한 이메일이 이미 존재하면 회원가입 실패
        if (read.isPresent()) {
            throw new IllegalStateException("이미 가입된 회원입니다");
        }

        // 입력한 이메일이 존재하지 않으면 회원가입 성공
        String password = passwordEncoder.encode(memberDTO.getMemberPassword());
        Member member = modelMapper.map(memberDTO, Member.class);
        member.setMemberName(memberDTO.getMemberName());
        member.setMemberPhone(memberDTO.getMemberPhone());
        member.setMemberPassword(password); // 암호화된 비밀번호 저장
        member.setMemberStatus("활성"); // 회원가입시 memberStatus 는 기본적으로 '활성' 상태로 가입
        member.setRole(Role.MANAGER); // MANAGER 권한으로 가입

        // Member 저장 후 반환
        return memberRepository.save(member);  // 저장된 Member 객체 반환
    }

    // 이메일 중복 확인
    public boolean isEmailExists(String email) {
        Optional<Member> member = memberRepository.findByMemberEmail(email);
        return member.isPresent(); // 이미 존재하면 true 반환
    }

    // 회원정보 수정
    public Member update(MemberDTO memberDTO) {
        // 현재 로그인한 사용자 이메일로 조회
        Optional<Member> optionalUser = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        if (optionalUser.isPresent()) {
            Member member = optionalUser.get();

            // 현재 비밀번호 확인
            if (memberDTO.getCurrentPassword() != null && !passwordEncoder.matches(memberDTO.getCurrentPassword(), member.getMemberPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호 확인
            if (memberDTO.getNewPassword() != null && !memberDTO.getNewPassword().isEmpty()) {
                if (memberDTO.getNewPassword().length() < 6) {
                    throw new IllegalArgumentException("새 비밀번호는 6글자 이상이어야 합니다.");
                }
                if (!memberDTO.getNewPassword().equals(memberDTO.getNewPasswordCheck())) {
                    throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
                }
                // 새 비밀번호로 업데이트
                member.setMemberPassword(passwordEncoder.encode(memberDTO.getNewPassword()));
            }

            // 이름, 전화번호도 업데이트
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

    // 호텔장에 의한 회원정보 수정
    public Member managerUpdate(MemberDTO memberDTO) {
        // 수정하려는 회원 정보를 찾기
        Member member = memberRepository.findById(memberDTO.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 수정하려는 회원의 role이 'ADMIN' 또는 'USER'인지 확인
        if ("ADMIN".equals(member.getRole()) || "USER".equals(member.getRole())) {
            throw new IllegalArgumentException("ADMIN 또는 USER 역할을 가진 회원은 수정할 수 없습니다.");
        }

        member.setRole(memberDTO.getRole());
        member.setMemberStatus(memberDTO.getMemberStatus());

        return memberRepository.save(member);
    }

    // 회원정보 개별조회
    public MemberDTO read(String memberEmail) {
        Optional<Member> user = memberRepository.findByMemberEmail(memberEmail);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("해당 이메일의 회원을 찾을 수 없습니다.");
        }

        return modelMapper.map(user.get(), MemberDTO.class);
    }

    // 회원탈퇴
    @Transactional
    public void delete(String memberEmail, String currentPassword, RedirectAttributes redirectAttributes) {
        // 회원 존재 여부 확인
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원을 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getMemberPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호가 일치하면 회원이 작성한 Reply 데이터를 먼저 삭제
        replyRepository.deleteByMember(member);

        // 비밀번호가 일치하면 회원이 작성한 QNA 데이터를 먼저 삭제
        qnaRepository.deleteByMember(member);

        // 비밀번호가 일치하면 회원에게 온 알림 데이터를 먼저 삭제
        notificationRepository.deleteByMember(member);

        // 비밀번호가 일치하면 호텔장 회원이라면 해당 호텔 레코드를 삭제
        List<Hotel> hotels = hotelRepository.findByMember(member);
        for (Hotel hotel : hotels) {
            // 호텔의 memberId와 회사의 memberId가 같을 경우, 호텔 레코드를 삭제
            if (hotel.getCompany().getMember().getMemberId().equals(member.getMemberId())) {
                hotelRepository.delete(hotel);
            } else {
                // 그렇지 않으면 hotel의 memberId를 해당 회사의 memberId로 업데이트
                Member companyMember = hotel.getCompany().getMember();

                // 회사의 Member가 영속 상태가 아닐 경우 저장
                if (!memberRepository.existsById(companyMember.getMemberId())) {
                    memberRepository.save(companyMember); // Member 객체 먼저 저장
                }

                hotel.setMember(companyMember); // Hotel에 member 설정
                hotelRepository.save(hotel); // Hotel 객체 저장
            }
        }

        // 비밀번호가 일치하면 회원이 등록한 company 데이터를 먼저 삭제
        companyRepository.deleteByMember(member);

        // 비밀번호가 일치하면 회원 삭제
        memberRepository.delete(member);

        // 회원 탈퇴 완료 후 메시지 추가
        redirectAttributes.addFlashAttribute("successMessage", "회원탈퇴가 완료되었습니다. 😢");
    }

    // 전체 회원 조회 및 검색기능
    public List<MemberDTO> getFilteredMembers(String role, String email, String status,
                                              String name, String phone, String startDate, String endDate) {
        List<Member> members = memberRepository.findAll();

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

    public Member readByMembername(String memberEmail) {
        Optional<Member> member = memberRepository.findByMemberEmail(memberEmail);
        return member.orElse(null);  // 없으면 null 반환
    }

    public Member findById(Integer memberId) {
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isPresent()) {
            return memberOpt.get();
        } else {
            throw new RuntimeException("Member를 찾을 수 없습니다.");
        }
    }
}
