package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.RoomReview;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RoomReviewDTO {

    private Integer reviewId;

    private Integer rating;

    @Size(max = 255, message = "리뷰 내용은 최대 255자까지 입력 가능합니다.")
    private String reviewText;

    private Integer roomId;

    private Integer memberId;

    private String roomName;

    private String memberName;

    private LocalDateTime reviewDate;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    public static RoomReviewDTO fromEntity(RoomReview roomReview) {
        if (roomReview == null) {
            throw new IllegalArgumentException("roomReview 엔티티가 null입니다.");
        }

        return new RoomReviewDTO(
                roomReview.getReviewId(),
                roomReview.getRating(),
                roomReview.getReviewText(),
                roomReview.getRoom() != null ? roomReview.getRoom().getRoomId() : null,
                roomReview.getMember() != null ? roomReview.getMember().getMemberId() : null,
                roomReview.getRoom() != null ? roomReview.getRoom().getRoomName() : "삭제된 객실",
                roomReview.getMember() != null ? roomReview.getMember().getMemberName() : "탈퇴한 회원",
                roomReview.getReviewDate(),
                roomReview.getRegDate(),
                roomReview.getModDate()
        );
    }

}
