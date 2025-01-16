package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="roomReview")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomReview extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;
    // 평점
    @Column(nullable = false)
    private Integer rating;
    // 리뷰 내용
    @Column(length = 255)
    private String reviewText;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room roomId;

    @ManyToOne
    @JoinColumn(name = "usersId")
    private Users usersId;


}
