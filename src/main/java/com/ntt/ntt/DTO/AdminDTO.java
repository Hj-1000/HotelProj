package com.ntt.ntt.DTO;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.Entity.Admin;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AdminDTO {

    private Integer adminId;

    @NotBlank
    @Size(min = 2, max = 20, message = "이름은 2~20글자 사이로 입력해주세요.")
    private String adminName;

    @NotBlank
    @Size(min = 2, max = 50)
    @Email(message = "이메일 형식에 맞춰서 입력해주세요")
    private String adminEmail;

    @NotBlank
    @Size(min = 6, max = 20, message = "비밀번호는 6~20글자 사이로 입력해주세요.")
    private String adminPassword;

    private String adminPhone;

    private Role role;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
