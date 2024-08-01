package com.ormi.happyhouse.post.service;

import com.ormi.happyhouse.member.domain.Users;

import com.ormi.happyhouse.post.domain.Comment;
import com.ormi.happyhouse.post.domain.Post;
import com.ormi.happyhouse.post.dto.CommentDto;
import com.ormi.happyhouse.post.repository.CommentRepository;
import com.ormi.happyhouse.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    //private final UserRepository userRepository;
    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        //this.userRepository = userRepository;
    }

    // Create: 댓글 생성 메서드
    /*public void saveComment(Long postId, Long userId, String content) {
        Optional<Post> postRepositoryById = postRepository.findById(postId);
        Post post = postRepositoryById.orElseThrow(() -> new RuntimeException("Post not found"));
        Optional<Users> userRepositoryById = userRepository.findById(userId);
        Users user = userRepositoryById.orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = new Comment().builder()
                .post(post)
                .users(user)
                .content(content)
                .createdAt(new Date())
                .build();
        commentRepository.save(comment);
    }*/
/*
    // Delete: 댓글 삭제 메서드
    public CommentDto deleteComment(Long commentId) {
        Optional<Comment> commentById = commentRepository.findById(commentId);
        Comment comment = commentById.orElseThrow(() -> new RuntimeException("Comment not found"));
        Comment deletedComment = new Comment().builder()
                .commentId(comment.getCommentId())
                .post(comment.getPost())
                .users(comment.getUsers())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .deleteYn(true)
                .build();
        commentRepository.save(deletedComment);
        return CommentDto.fromEntity(deletedComment);
    }*/
}
