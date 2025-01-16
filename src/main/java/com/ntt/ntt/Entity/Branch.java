package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="branch")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Branch extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer branchId;
    // 지사명
    @Column(length = 50, nullable = false)
    private String branchName;
    // 지사장 이름
    @Column(length = 20)
    private String branchManager;

    @ManyToOne
    @JoinColumn(name = "chiefId")
    private Chief chiefId;

}
