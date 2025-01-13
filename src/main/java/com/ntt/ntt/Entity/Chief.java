// 호텔장 테이블

package com.ntt.ntt.Entity;

import com.ntt.ntt.Constant.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="chief")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chief extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer chiefId;
    // 호텔장 이름
    @Column(length = 20, nullable = false)
    private String chiefName;
    // 호텔장 이메일(고유값)
    @Column(length = 50, unique = true , nullable = false)
    private String chiefEmail;
    // 호텔장 비밀번호
    @Column(length = 20, nullable = false)
    private String chiefPassword;
    // 호텔장 전화번호
    @Column(length = 20)
    private String chiefPhone;
    // 권한
    @Enumerated(EnumType.STRING)
    private Role role;

}
