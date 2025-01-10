package com.ntt.ntt.Entity;

import com.ntt.ntt.Constant.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="admin")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Admin extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer adminId;
    // 어드민 이름
    @Column(length = 20, nullable = false)
    private String adminName;
    // 어드민 이메일(고유값)
    @Column(length = 50, unique = true, nullable = false)
    private String adminEmail;
    // 어드민 비밀번호
    @Column(length = 20, nullable = false)
    private String adminPassword;
    // 어드민 전화번호
    @Column(length = 20)
    private String adminPhone;
    // 권한
    @Enumerated(EnumType.STRING)
    private Role role;

}
