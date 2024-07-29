package com.ormi.happyhouse.member.controller;

import com.ormi.happyhouse.member.dto.UserDto;
import com.ormi.happyhouse.member.exception.LoginException;
import com.ormi.happyhouse.member.exception.UserRegistrationException;
import com.ormi.happyhouse.member.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class UserController {

    private final UserService userService;

    //회원가입 페이지 이동
    @GetMapping("/register")
    public String showSignup(Model model){
        model.addAttribute("user", new UserDto());
        return "users/register";
    }
    //회원가입 처리
    @PostMapping("/register")
    public String signup(@ModelAttribute UserDto userDto, RedirectAttributes redirectAttributes){
        try{
            log.info("userDto email: {}", userDto.getEmail());
            userService.register(userDto);
            return "redirect:/member/login";
        }catch (UserRegistrationException ure){
            log.error("회원가입 실패 ", ure);
            redirectAttributes.addFlashAttribute("errorMsg", ure.getMessage());
            //redirectAttributes.addAttribute("errorMsg", ure.getMessage());
            return "redirect:/member/register";
        }
    }
    //로그인 페이지 이동
    @GetMapping("/login")
    public String showLogin(){
        return "users/login";
    }

    //닉네임 중복 체크
    @CrossOrigin(origins = "*")
    @GetMapping("/duplicateNickname")
    @ResponseBody
    public ResponseEntity<Boolean> checkDuplicatedNickname(@RequestParam("nickname") String nickname){
        log.info("중복 확인 {}", nickname);
        boolean isDuplicated = userService.isDuplicatedNickname(nickname);
        return isDuplicated ? ResponseEntity.status(HttpStatus.CONFLICT).body(false) : ResponseEntity.ok(isDuplicated);
    }
}