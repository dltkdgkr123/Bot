package com.sh.bdt.thymeleaf.repository;


import com.sh.bdt.entity.Post;
import com.sh.bdt.thymeleaf.repository.querydsl.PostViewRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends JpaRepository<Post, Long>, PostViewRepositoryCustom {

}
