package com.ntt.ntt.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @CreatedDate // 만든날짜
    @Column(name="regDate", nullable = false, updatable = false)
    private LocalDateTime regDate;

    @LastModifiedDate // 수정한날짜
    @Column(name="modDate")
    private LocalDateTime modDate;

}
