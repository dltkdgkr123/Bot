package com.sh.bdt.entity;

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

  @Getter @Id @GeneratedValue private Long id;

  @Getter private int likeCount;

  public void increaseLike() {
    this.likeCount++;
  }

  public static Post create() { // for dummy
    return new Post(null, 0);
  }
}
