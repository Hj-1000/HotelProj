package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="company")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer companyId;
    // 본사 이름
    @Column(length = 50, nullable = false)
    private String companyName;
    // 본사 관리자명
    @Column(length = 20)
    private String companyManager;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Image> companyImageList;

    //2024-02-10 양방향 삭제를 위해 추가
    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @ToString.Exclude  // hotels 리스트를 toString()에서 제외
    private List<Hotel> hotels;


    public Company(Integer companyId) {
        this.companyId = companyId;
    }

}
