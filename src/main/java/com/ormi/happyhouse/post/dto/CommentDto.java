package com.ormi.happyhouse.post.dto;

import com.ormi.happyhouse.member.domain.Users;
import com.ormi.happyhouse.post.domain.Comment;
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
public class CommentDto {

    private Long commentId;

    private Post post;

    private Users users;

    private String content;

    private Date createdAt;

    private boolean deleteYn;

//    public Comment toEntity(){
//        return new Comment().builder()
//                .post(this.post)
//                .users(this.users)
//                .content(this.content)
//                .createdAt(this.createdAt)
//                .deleteYn(this.deleteYn)
//                .build();
//    }

    public static CommentDto fromEntity(Comment comment){
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentId(comment.getCommentId());
        commentDto.setPost(comment.getPost());
        commentDto.setUsers(comment.getUsers());
        commentDto.setContent(comment.getContent());
        commentDto.setCreatedAt(comment.getCreatedAt());
        commentDto.setDeleteYn(comment.isDeleteYn());
        return commentDto;
    }
}