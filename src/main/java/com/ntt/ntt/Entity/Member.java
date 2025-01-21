package com.ntt.ntt.Entity;

import com.ntt.ntt.Constant.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="member")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberId;
    //  이름
    @Column(length = 20, nullable = false)
    private String memberName;
    // 사용자 이메일(고유값)
    @Column(length = 50, unique = true, nullable = false)
    private String memberEmail;
    // 사용자 비밀번호
    @Column(nullable = false)
    private String memberPassword;
    // 사용자 전화번호
    @Column(length = 20)
    private String memberPhone;
    // 권한
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member")  // 이 부분에서 mappedBy를 사용하여 양방향 관계를 관리
    private List<Qna> qnas;

    @OneToMany(mappedBy = "member")  // 이 부분은 Reply와 관련된 부분
    private List<Reply> replies;

}
