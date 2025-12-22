package com.sh.bdt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "post_like",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"post_id", "user_id"})})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTime {

  @Getter @Id @GeneratedValue private Long id;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  private Post post;

  @Getter private Long userId;

  public PostLike(Post post, Long userId) {
    this.post = post;
    this.userId = userId;
  }
}
