package com.ormi.happyhouse.post.controller;

import com.ormi.happyhouse.post.domain.Comment;
import com.ormi.happyhouse.post.dto.CommentDto;
import com.ormi.happyhouse.post.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // Create: 댓글 생성
    @PostMapping("/{post_id}")
    public ResponseEntity<?> saveComment(
            @PathVariable("post_id") Long postId,
            @RequestParam("content") String content,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("/comment/{post_id} POST 요청");
        try{
            commentService.saveComment(postId, content, authHeader);
//            URI redirectUri = new URI("/post/" + postId);
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.setLocation(redirectUri);
//            return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND); // 성공 시 302, postId값으로 리다이렉트
            return ResponseEntity.status(HttpStatus.OK).location(URI.create("/post/" + postId)).build();
        }catch (Exception e){
            log.error("댓글 저장 중 에러",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 저장 중 에러 :"+e.getMessage());
        }
    }

    // Delete: 댓글 삭제
    @PutMapping("/delete/{comment_id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("comment_id") Long commentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("/delete/{comment_id} POST 요청");
        try{
            Long postId = commentService.deleteComment(commentId, authHeader);
            URI redirectUri = new URI("/post/" + postId);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(redirectUri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND); // 성공 시 302, postId값으로 리다이렉트
        }catch (Exception e){
            log.error("댓글 삭제 중 에러",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 중 에러 :"+e.getMessage());
        }
    }
}
