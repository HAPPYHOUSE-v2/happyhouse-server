package com.ormi.happyhouse.post.service;

import com.ormi.happyhouse.post.domain.Post;
import com.ormi.happyhouse.post.dto.PostDto;
import com.ormi.happyhouse.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // 게시글 생성 메서드
    public void savePost(PostDto postDto) {
        postDto.setCreatedAt(new Date());
        postDto.setViewCount(0L);
        postRepository.save(postDto.toEntity());
    }

    // 게시글 목록 조회 메서드
    public Page<PostDto> showAllPost(String title, Pageable pageable) {
        Page<Post> postListPage = postRepository.findByDeleteYnFalseAndTitleContains(title, pageable);
        return postListPage.map(PostDto::fromEntity);
    }

    // 게시글 상세 조회 메서드
    public PostDto showPostDetail(Long postId) {
        Optional<Post> postById = postRepository.findById(postId);

        // view_count 1 증가
        Post post = postById.orElseThrow(()-> new IllegalArgumentException("post not found"));
        Post viewCountPlusPost = viewCountPlusPost(post);
        postRepository.save(viewCountPlusPost);

        return PostDto.fromEntity(viewCountPlusPost);
    }

    // viewCount 1증가 시키는 메서드
    public Post viewCountPlusPost(Post post) {
        return new Post().builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount() + 1)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .noticeYn(post.isNoticeYn())
                .deleteYn(post.isDeleteYn())
                .build();
    }
}
