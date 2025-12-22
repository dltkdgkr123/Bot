package com.sh.bdt.dummy;

import com.sh.bdt.entity.Post;
import com.sh.bdt.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "dummy.loader.mysql.enabled",
    havingValue = "true",
    matchIfMissing = false)
@RequiredArgsConstructor
public class PostDummyLoader implements CommandLineRunner {

  private final PostRepository postRepository;

  @Override
  public void run(String... args) {
    if (!postRepository.existsById(1L)) {
      Post post = Post.create();
      postRepository.save(post);
    }
  }
}
