package com.ormi.happyhouse.member.controller;

import com.ormi.happyhouse.member.dto.LoginRequestDto;
import com.ormi.happyhouse.member.dto.ModifyUserInfoRequest;
import com.ormi.happyhouse.member.dto.UserDto;
import com.ormi.happyhouse.member.jwt.JwtUtil;
import com.ormi.happyhouse.member.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    //마이페이지 이동
    @GetMapping
    public String showMypage(@RequestParam(name="menu", defaultValue = "profile") String menu, Model model) {
        model.addAttribute("selectedMenu", menu);
        return "users/mypage";
    }

    //개인정보 수정 - 유저 정보 조회
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader){
        log.info("/userinfo header :{}", authHeader);
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            String email  = jwtUtil.getEmailFromToken(authHeader.replace("Bearer ", ""));
            UserDto userInfo = UserDto.builder().nickname(userService.findNicknameByEmail(email)).build();
            return ResponseEntity.ok(userInfo);
        }else{
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("토큰이 만료되었습니다.");
        }
        /*log.info("/userinfo header :{}", authHeader);
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            try {
                String token = authHeader.replace("Bearer ", "");
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.getEmailFromToken(token);
                    UserDto userInfo = UserDto.builder().nickname(userService.findNicknameByEmail(email)).build();
                    return ResponseEntity.ok(userInfo);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid");
        }*/
    }
    //개인정보 수정
    @PutMapping ("/modifyInfo")
    public ResponseEntity<?> modifyUserInfo(@RequestBody ModifyUserInfoRequest modifyUserInfoReq,
                                            @RequestHeader(value = "Authorization") String authHeader){
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);

            try {
                UserDto updatedUser = userService.modifyUserInfo(email, modifyUserInfoReq);
                return ResponseEntity.ok(updatedUser);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

}
