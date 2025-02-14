package com.ntt.ntt.DTO;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.Entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {

    private Integer memberId;

    @NotBlank
    @Size(min = 2, max = 20, message = "이름은 2~20글자 사이로 입력해주세요.")
    private String memberName;

    @NotBlank
    @Size(min = 2, max = 50)
    @Email(message = "이메일 형식에 맞춰서 입력해주세요")
    private String memberEmail;

    @NotBlank
    @Size(min = 6, max = 20)
    @Size(message = "비밀번호는 6~20글자 사이로 입력해주세요.")
    private String memberPassword;

    private String memberPhone;

    private String memberStatus;

    private Role role;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    // 현재 비밀번호
    private String currentPassword;

    // 새 비밀번호
    private String newPassword;

    // 새 비밀번호 확인
    private String newPasswordCheck;

    public MemberDTO(Member member) {
        this.memberId = member.getMemberId();
        this.memberName = member.getMemberName();
        this.memberEmail = member.getMemberEmail();
        this.memberPassword = member.getMemberPassword();
        this.memberPhone = member.getMemberPhone();
        this.memberStatus = member.getMemberStatus();
        this.role = member.getRole();
        this.regDate = member.getRegDate();
        this.modDate = member.getModDate();
    }

}
