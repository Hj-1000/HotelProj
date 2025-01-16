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
    // 지사명
    @Column(length = 50, nullable = false)
    private String companyName;
    // 지사장 이름
    @Column(length = 20)
    private String companyManager;

    @ManyToOne
    @JoinColumn(name = "chiefId")
    private Chief chiefId;

}
