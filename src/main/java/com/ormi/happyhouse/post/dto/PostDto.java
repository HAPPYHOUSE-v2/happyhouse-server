package com.ormi.happyhouse.post.dto;

import com.ormi.happyhouse.member.domain.Users;
import com.ormi.happyhouse.post.domain.Comment;
import com.ormi.happyhouse.post.domain.File;
import com.ormi.happyhouse.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

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

    private List<Comment> comments;

    private List<File> files;

    private Users user;

    public Post toEntity() {
        return new Post().builder()
//                .userId(this.getUserId())
                .title(this.getTitle())
                .content(this.getContent())
                .viewCount(this.getViewCount())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .deleteYn(this.isDeleteYn())
                .noticeYn(this.isNoticeYn())
                .comments(this.getComments())
                .files(this.getFiles())
                .user(this.getUser())
                .build();
    }

    public static PostDto fromEntity(Post post) {
        PostDto postDto = new PostDto();
        postDto.setPostId(post.getPostId());
//        postDto.setUserId(post.getUserId());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        postDto.setViewCount(post.getViewCount());
        postDto.setCreatedAt(post.getCreatedAt());
        postDto.setUpdatedAt(post.getUpdatedAt());
        postDto.setDeleteYn(post.isDeleteYn());
        postDto.setNoticeYn(post.isNoticeYn());
        postDto.setComments(post.getComments());
        postDto.setFiles(post.getFiles());
        postDto.setUser(post.getUser());
        return postDto;
    }
}
