package com.ormi.happyhouse.post.controller;

import com.ormi.happyhouse.post.domain.Comment;
import com.ormi.happyhouse.post.dto.CommentDto;
import com.ormi.happyhouse.post.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // Create: 댓글 생성
    @PostMapping("/{post_id}")
    public String saveComment(
            @PathVariable("post_id") Long post_id,
            @RequestParam("user_id") Long user_id,
            @RequestParam("content") String content
    ) {
        commentService.saveComment(post_id, user_id, content);
        return "redirect:/post/" + post_id;
    }

    // Delete: 댓글 삭제
    @PutMapping("/delete/{comment_id}")
    public String deleteComment(@PathVariable("comment_id") Long commentId, Model model) {
        CommentDto deletedComment = commentService.deleteComment(commentId);
        return "redirect:/post/" + deletedComment.getPost().getPostId();
    }
}
