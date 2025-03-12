package com.ntt.ntt.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ntt.ntt.Constant.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="member")
@Getter
@Setter
@ToString(exclude = {"qnaList", "notifications", "replies"}) // 연관 관계 필드 제외
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
    // 사용자 상태 (활성/비활성)
    private String memberStatus;
    // 권한
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // 무한 참조 방지
    private List<Qna> qnaList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // 무한 참조 방지
    private List<Notification> notifications;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // 무한 참조 방지
    private List<Reply> replies;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Likes likes;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private ServiceCart serviceCart;
}
