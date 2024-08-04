package com.ormi.happyhouse.post.controller;

import com.ormi.happyhouse.member.jwt.JwtUtil;
import com.ormi.happyhouse.post.dto.PostDto;
import com.ormi.happyhouse.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Controller
@RequestMapping("/post")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    // Create: 게시글 생성
    @PostMapping
    public ResponseEntity<?> savePost(
            @ModelAttribute PostDto postDto,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) throws IOException {

        log.info("/post POST 요청");
        try{
            postService.savePost(postDto, file, authHeader);
            return ResponseEntity.ok().build(); //성공 시 200
        }catch (Exception e){
            log.error("게시글 저장 중 에러",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 저장 중 에러 :"+e.getMessage());
        }
    }

    // Read: 게시글 목록 조회
    @GetMapping
    public String showAllPost(
            Model model,
            @RequestParam(defaultValue = "", value = "title", required = false) String title,
            @PageableDefault
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "noticeYn", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            })
            Pageable pageable
    ) {
        Page<PostDto> posts = postService.showAllPost(title, pageable);

        int nowPage = posts.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 2, 1);
        int endPage = Math.min(startPage + 4, posts.getTotalPages());

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPage", posts.getTotalPages());
        model.addAttribute("searchTitle", title);

        return "post/list";
    }

    // Read: 게시글 상세 조회
    @GetMapping("/{post_id}")
    public String showPostDetail(
            @PathVariable("post_id") Long postId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Model model
    ) {
        log.info("상세 조회");

        PostDto post = postService.showPostDetail(postId);
        boolean isYourPost = postService.isYourPost(postId, authHeader);
        String yourEmail = postService.yourEmail(authHeader);

        model.addAttribute("post", post);
        model.addAttribute("isYourPost", isYourPost);
        model.addAttribute("yourEmail", yourEmail);
        return "post/detail";

    }

    // Read: 게시글 작성 페이지로 이동
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("isEdit", false);
        return "post/form";
    }

    // Read: 게시글 수정 페이지로 이동
    @GetMapping("/edit/{post_id}")
    public String showEditForm(@PathVariable("post_id") Long postId, Model model) {
        PostDto post = postService.showPostDetail(postId);
        model.addAttribute("post", post);
        model.addAttribute("isEdit", true);
        return "post/form";
    }

    // Update: 게시글 수정
    @PutMapping("/{post_id}")
    public ResponseEntity<?> updatePost(
            @PathVariable("post_id") Long postId,
            @ModelAttribute PostDto postDto,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Model model) throws IOException {

        log.info("/post/{post_id} PUT 요청");
        try{
            postService.updatePost(postId, postDto, file);
//            URI redirectUri = new URI("/post/" + postId);
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.setLocation(redirectUri);
//            return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND); // 성공 시 302, postId값으로 리다이렉트
            boolean isYourPost = postService.isYourPost(postId, authHeader);
            String yourEmail = postService.yourEmail(authHeader);

            model.addAttribute("isYourPost", isYourPost);
            model.addAttribute("yourEmail", yourEmail);
            return ResponseEntity.status(HttpStatus.OK).location(URI.create("/post/" + postId)).build(); //성공 시 200
            //return ResponseEntity.ok().build(); //성공 시 200
            //return "redirect:/post/" + postId; // 성공 시 200
        }catch (Exception e){
            log.error("게시글 수정 중 에러",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 수정 중 에러 :"+e.getMessage());
            //return "error/401";
        }
    }

    // Delete: 게시글 삭제
    @PutMapping("/delete/{post_id}")
    public String deletePost(
            @PathVariable("post_id") Long postId, 
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {

        log.info("/delete/{post_id} PUT 요청");
        try{
            postService.deletePost(postId, authHeader);
            return "redirect:/post/" + postId; // 성공 시 200
        }catch (Exception e){
            log.error("게시글 저장 중 에러",e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 저장 중 에러 :"+e.getMessage());
            return "error/401";
        }
    }
}
