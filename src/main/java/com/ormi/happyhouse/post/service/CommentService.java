package com.ormi.happyhouse.post.service;

import com.ormi.happyhouse.member.domain.Users;

import com.ormi.happyhouse.member.jwt.JwtUtil;
import com.ormi.happyhouse.member.repository.UsersRepository;
import com.ormi.happyhouse.post.domain.Comment;
import com.ormi.happyhouse.post.domain.Post;
import com.ormi.happyhouse.post.dto.CommentDto;
import com.ormi.happyhouse.post.repository.CommentRepository;
import com.ormi.happyhouse.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final JwtUtil jwtUtil;


    // Create: 댓글 생성 메서드
    public void saveComment(Long postId, String content, String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);


        Optional<Post> postRepositoryById = postRepository.findById(postId);
        Post post = postRepositoryById.orElseThrow(() -> new RuntimeException("Post not found"));
        Optional<Users> userRepositoryById = usersRepository.findByEmail(email);
        Users user = userRepositoryById.orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = new Comment().builder()
                .post(post)
                .users(user)
                .content(content)
                .createdAt(new Date())
                .build();
        commentRepository.save(comment);
    }

    // Delete: 댓글 삭제 메서드
    public Long deleteComment(Long commentId, String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);

        Optional<Comment> commentById = commentRepository.findById(commentId);
        Comment comment = commentById.orElseThrow(() -> new RuntimeException("Comment not found"));

        if(email.equals(comment.getUsers().getEmail())) {
            Comment deletedComment = new Comment().builder()
                    .commentId(comment.getCommentId())
                    .post(comment.getPost())
                    .users(comment.getUsers())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .deleteYn(true)
                    .build();
            commentRepository.save(deletedComment);

            return deletedComment.getPost().getPostId();
        } throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
    }
}
