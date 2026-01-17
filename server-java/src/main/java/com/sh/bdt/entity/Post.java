package com.sh.bdt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTime {

    @Getter
    @Id
    @GeneratedValue
    private Long id;

    @Getter
    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Getter
    @Column(name = "batched_at")
    private long batchedAt;

    public void increaseLike() {
        this.likeCount++;
    }

    public static Post create() { // 안티 패턴: 엔티티에 더미, 테스트용 메서드 주입 (실험을 위해 채택)
        return new Post(null, 0, 0L);
    }
}
