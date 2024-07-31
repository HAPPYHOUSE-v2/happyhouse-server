package com.ormi.happyhouse.post.controller;

import com.ormi.happyhouse.post.dto.PostDto;
import com.ormi.happyhouse.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/post")
public class PostController {

    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public String savePost(@RequestBody PostDto postDto, Model model) {
        postService.savePost(postDto);
        return "redirect:/post";
    }

    @GetMapping
    public String showAllPost(
            Model model
            ,
            @PageableDefault
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "noticeYn", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            })
            Pageable pageable
    ) {
        Page<PostDto> posts = postService.showAllPost(pageable);

        int nowPage = posts.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 2, 1);
        int endPage = Math.min(startPage + 4, posts.getTotalPages());

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPage", posts.getTotalPages());
        model.addAttribute("isSearchPage", false);

        return "post/list";
    }
}
