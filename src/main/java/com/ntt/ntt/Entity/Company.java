package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

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
    private Member memberId;

}
