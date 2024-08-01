package com.ormi.happyhouse.member.controller;

import com.ormi.happyhouse.member.dto.*;
import com.ormi.happyhouse.member.exception.InvalidRefreshTokenException;
import com.ormi.happyhouse.member.exception.LoginException;
import com.ormi.happyhouse.member.exception.UserRegistrationException;
import com.ormi.happyhouse.member.jwt.JwtUtil;
import com.ormi.happyhouse.member.service.EmailService;
import com.ormi.happyhouse.member.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

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
    //닉네임 중복 체크
    @CrossOrigin(origins = "*")
    @GetMapping("/duplicateNickname")
    @ResponseBody
    public ResponseEntity<Boolean> checkDuplicatedNickname(@RequestParam("nickname") String nickname){
        log.info("중복 확인 {}", nickname);
        boolean isDuplicated = userService.isDuplicatedNickname(nickname);
        return isDuplicated ? ResponseEntity.status(HttpStatus.CONFLICT).body(false) : ResponseEntity.ok(isDuplicated);
    }
    //이메일 인증
    @CrossOrigin(origins = "*")
    @ResponseBody
    @PostMapping("/send-verification-email")
    public ResponseEntity<String> sendVerificationEmail(@RequestParam("email") String email) {
        log.info("send-verification-email :{}", email);
        emailService.sendVerificationEmail(email);
        return ResponseEntity.ok("인증 이메일이 전송되었습니다.");
    }
    //이메일 인증(회원가입 시)
    @ResponseBody
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> mailCode) {
        String email = mailCode.get("email");
        String code = mailCode.get("code");
        log.info("인증 이메일 :{}", email);
        log.info("인증 코드 :{}", code);
        boolean isVerified = emailService.verifyEmailCode(email, code);
        if (isVerified) {
            return ResponseEntity.ok("이메일이 성공적으로 인증되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 올바르지 않습니다.");
        }
    }

    //로그인 페이지 이동
    @GetMapping("/login")
    public String showLogin(){
        return "users/login";
    }

    //jwt 로그인 처리
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto,
                                   HttpServletResponse response) {
        try {
            LoginResponseDto loginResponseDto = userService.loginWithJwt(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            // Refresh Token을 쿠키에 추가
            response.addCookie(loginResponseDto.getRefreshTokenCookie());
            // Access Token을 Authorization 헤더에 추가
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponseDto.getAccessToken());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(Map.of("message","Login successful"));
        } catch (BadCredentialsException bce) {
            //비밀번호가 틀린 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "이메일 또는 비밀번호가 옳지 않습니다."));
        } catch (UsernameNotFoundException unfe){
            // 사용자를 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "해당 계정을 찾을 수 없습니다."));
        }catch (Exception e){
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "로그인 오류"));
        }
    }
    //JWT 로그아웃 처리 : 서버에서 리프레시 토큰 삭제, 클라이언트에서 리프레시 쿠키 무효화
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogOutRequestDto logOutReq, HttpServletResponse response) {
        userService.setLogoutCookie(logOutReq.getEmail(), response);
        return ResponseEntity.ok().body("로그아웃 성공");
    }
    @PostMapping("/refresh") //새로운 액세스 토큰을 응답 본문에 포함하고 새 리프레시 토큰 생성
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refresh_token", required = false) String refreshToken,
                                          HttpServletResponse response) {
        log.info("/refresh 요청 :{}", refreshToken);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing");
        }
        try {
            String email = jwtUtil.getEmailFromToken(refreshToken); // JWT에서 이메일 추출
            LoginResponseDto refreshResponse = userService.refreshToken(email, refreshToken);
            // Refresh Token을 쿠키에 추가
            response.addCookie(refreshResponse.getRefreshTokenCookie());
            // Access Token을 응답 본문에 포함
            return ResponseEntity.ok().body(Map.of("accessToken", refreshResponse.getAccessToken()));
        } catch (InvalidRefreshTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }
    //JWT 클라이언트 인증 상태 확인 / JWT 토큰 유효성 검증
    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("check-auto 요청");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 이후의 토큰 추출
            try {
                log.info("jwtUtil에서 토큰 유효 확인 :{}", token);
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.getEmailFromToken(token);
                    log.info("토큰에서 메일 추출 :{}", email);
                    String nickname = userService.findNicknameByEmail(email);
                    return ResponseEntity.ok(Map.of("isLoggedIn", true, "email", email, "nickname", nickname));
                }
            } catch (ExpiredJwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("isLoggedIn", false, "error", "Token expired"));
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("isLoggedIn", false, "error", "Invalid token"));
            }
            log.info("check-auto token :{}", token);
        }
        return ResponseEntity.ok(Map.of("isLoggedIn", false));
    }

    //비밀번호 찾기 - 임시 비밀번호 생성
    @PostMapping("/temppassword")
    public ResponseEntity<?> createTempPassword(@RequestBody ResetPwReqeust resetPwReq) {
        log.info("/temppassword");
        String email = resetPwReq.getEmail();
        try {
            String tempPassword = userService.resetPassword(email);
            emailService.sendResetPassword(email, tempPassword);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }


}