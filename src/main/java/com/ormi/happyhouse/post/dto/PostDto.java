package com.ormi.happyhouse.post.dto;

import com.ormi.happyhouse.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private Long postId;

    private Long userId;

    private String title;

    private String content;

    private Long viewCount;

    private Date createdAt;

    private Date updatedAt;

    private boolean deleteYn;

    private boolean noticeYn;


    public Post toEntity() {
        return new Post().builder()
                .userId(this.getUserId())
                .title(this.getTitle())
                .content(this.getContent())
                .viewCount(this.getViewCount())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .deleteYn(this.isDeleteYn())
                .noticeYn(this.isNoticeYn())
                .build();
    }

    public static PostDto fromEntity(Post post) {
        PostDto postDto = new PostDto();
        postDto.setPostId(post.getPostId());
        postDto.setUserId(post.getUserId());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        postDto.setViewCount(post.getViewCount());
        postDto.setCreatedAt(post.getCreatedAt());
        postDto.setUpdatedAt(post.getUpdatedAt());
        postDto.setDeleteYn(post.isDeleteYn());
        postDto.setNoticeYn(post.isNoticeYn());
        return postDto;
    }
}
