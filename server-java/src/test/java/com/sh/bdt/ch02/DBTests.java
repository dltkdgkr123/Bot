package com.sh.bdt.ch02;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sh.bdt.entity.Post;
import com.sh.bdt.entity.PostLike;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

@Tag("ch02")
@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DBTests {

  @Autowired TestEntityManager em;

  /*
   * Service layer prevents duplicate inserts via exists-check,
   * so unique constraint violations do not occur in normal cases.
   *
   * This test bypasses the service and forces write + flush
   * to verify the database-level unique constraint.
   */
  @Test
  @Transactional
  void shouldThrowException_whenDuplicatePostLikeInserted() {

    // given
    Post post = Post.create();
    em.persist(post);

    PostLike like1 = new PostLike(post, 100L);
    PostLike like2 = new PostLike(post, 100L); // duplicated write

    // when
    em.merge(like1);
    em.merge(like2);

    // then (flush triggers DB constraint)
    assertThatThrownBy(() -> em.flush())
        .isInstanceOf(
            jakarta.persistence.PersistenceException
                .class); // or ConstraintViolationException.class
  }
}
