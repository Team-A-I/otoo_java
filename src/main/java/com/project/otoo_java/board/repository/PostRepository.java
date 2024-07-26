package com.project.otoo_java.board.repository;

import com.project.otoo_java.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByPostIdDesc();
}
