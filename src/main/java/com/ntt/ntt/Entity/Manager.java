package com.ntt.ntt.Entity;

import com.ntt.ntt.Constant.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="manager")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Manager extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer managerId;
    // 매니저 이름
    @Column(length = 20, nullable = false)
    private String managerName;
    // 매니저 이메일
    @Column(length = 50, unique = true , nullable = false)
    private String managerEmail;
    // 매니저 비밀번호
    @Column(length = 20, nullable = false)
    private String managerPassword;
    // 매니저 전화번호
    @Column(length = 20)
    private String managerPhone;
    // 권한
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne
    @JoinColumn(name = "companyId")
    private Company companyId;

    @ManyToOne
    @JoinColumn(name = "chiefId")
    private Chief chiefId;

}
