package com.ormi.happyhouse.post.service;

import com.ormi.happyhouse.post.domain.Post;
import com.ormi.happyhouse.post.dto.PostDto;
import com.ormi.happyhouse.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    public void savePost(PostDto postDto) {
        postDto.setCreatedAt(new Date());
        postDto.setViewCount(0L);
        postRepository.save(postDto.toEntity());
    }

    public Page<PostDto> showAllPost(Pageable pageable) {
        Page<Post> allPostPage = postRepository.findByDeleteYn(false, pageable);
        return allPostPage.map(PostDto::fromEntity);
    }
}
