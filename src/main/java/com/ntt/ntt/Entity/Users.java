package com.ntt.ntt.Entity;

import com.ntt.ntt.Constant.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="users")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usersId;
    // 사용자 이름
    @Column(length = 20, nullable = false)
    private String userName;
    // 사용자 이메일
    @Column(length = 50, unique = true , nullable = false)
    private String userEmail;
    // 사용자 비밀번호
    @Column(length = 20, nullable = false)
    private String userPassword;
    // 사용자 전화번호
    @Column(length = 20)
    private String userPhone;

    @Enumerated(EnumType.STRING)
    private Role role;

}
