package com.project.otoo_java.board.service;

import com.project.otoo_java.board.entity.Post;
import com.project.otoo_java.board.dto.PostDto;
import com.project.otoo_java.board.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }
}