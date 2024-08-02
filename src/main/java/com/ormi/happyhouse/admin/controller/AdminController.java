package com.ormi.happyhouse.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    //Admin 페이지 이동 처리
    @GetMapping
    public String showAdminPage(@RequestParam(name="menu", defaultValue = "all") String menu, Model model){
        model.addAttribute("selectedMenu", menu);
        return "admin/main";
    }
    //Admin 전체 게시글 목록 조회

    //공지 게시글 조회

    //공지 게시글 등록

    //공지 게시글 수정

    //공지 게시글 상세 조회
}
