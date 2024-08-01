package com.ormi.happyhouse.member.service;

import com.ormi.happyhouse.member.dto.LoginResponseDto;
import com.ormi.happyhouse.member.dto.ModifyUserInfoRequest;
import com.ormi.happyhouse.member.exception.InvalidRefreshTokenException;
import com.ormi.happyhouse.member.exception.WithdrawnUserException;
import com.ormi.happyhouse.member.jwt.JwtUtil;
import com.ormi.happyhouse.member.domain.Provider;
import com.ormi.happyhouse.member.domain.UserRole;
import com.ormi.happyhouse.member.domain.Users;
import com.ormi.happyhouse.member.dto.UserDto;
import com.ormi.happyhouse.member.exception.UserRegistrationException;
import com.ormi.happyhouse.member.repository.UsersRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Random;

//Spring Security에서 사용자 정보를 가져오는 인터페이스
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UsersRepository usersRepository;
    //private final PasswordEncoder passwordEncoder; //순환참조
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final @Qualifier("stringRedisTemplate") StringRedisTemplate redisTemplate;

    // 회원가입
    @Transactional
    public void register(UserDto userDto) {
        // 이메일 검사
        if (!StringUtils.hasText(userDto.getEmail())) {
            throw new UserRegistrationException("이메일은 필수 입력 항목입니다.");
        }

        // 이메일 중복 체크 및 탈퇴 사용자 확인
        Optional<Users> existingUser = usersRepository.findByEmail(userDto.getEmail());
        if (existingUser.isPresent()) {
            Users user = existingUser.get();
            if (user.getStatus() == 2) {
                throw new UserRegistrationException("탈퇴한 사용자입니다.");
            } else {
                throw new UserRegistrationException("이미 사용 중인 이메일입니다.");
            }
        }

        // 이메일 인증 상태 확인
        Boolean isVerified = redisTemplate.opsForValue().get("EMAIL_VERIFIED:" + userDto.getEmail()) != null;
        if (!isVerified) {
            throw new UserRegistrationException("이메일 인증이 필요합니다.");
        }
        try {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            Users newUser =
                    Users.builder()
                            .email(userDto.getEmail())
                            .nickname(userDto.getNickname())
                            .password(passwordEncoder.encode(userDto.getPassword())) // 암호화 저장
                            .status(0)
                            .role(UserRole.USER)
                            .provider(Provider.LOCAL)
                            .build();
              usersRepository.save(newUser);
              // 인증 완료 후 Redis에서 인증 정보 삭제
             redisTemplate.delete("EMAIL_VERIFIED:" + userDto.getEmail());
        } catch (DataIntegrityViolationException de) {
            log.error("등록 중 오류 ", de);
            throw new UserRegistrationException("등록 중 오류 발생. 이메일이나 닉네임이 이미 사용 중일 수 있습니다.", de);
        } catch (Exception e) {
            log.error("Unexpected error ", e);
        }
    }
    //닉네임 중복확인
    public boolean isDuplicatedNickname(String nickname) {
        return usersRepository.existsByNickname(nickname);
    }
    @Override //SpringSecurity 기본 인증 매커니즘
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("load user {}", email);
        Users user = usersRepository.findByEmail(email)
              .orElseThrow(() -> new UsernameNotFoundException("User 정보가 없습니다: " + email));

        return org.springframework.security.core.userdetails.User.builder()
              .username(user.getEmail())
              .password(user.getPassword())
              .roles("USER")
              .build();
    }
    //JWT 로그인 (+ Refresh 토큰)
    public LoginResponseDto loginWithJwt(String email, String password){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        try{
            Users user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다: " + email));
            if(user.getStatus()==2){
                throw new WithdrawnUserException("탈퇴한 사용자입니다.");
            }
            UserDetails userDetails = loadUserByUsername(email);
            if(passwordEncoder.matches(password, userDetails.getPassword())){
                String accessToken = jwtUtil.generateAccessToken(email);
                String refreshToken = jwtUtil.generateRefreshToken(email);
                log.info("액세스ㅌ 토큰 확인 {}", accessToken);
                log.info("리프레시 토큰 확인 {}", refreshToken);
                //Refresh Token을 쿠키에 저장
                refreshTokenService.saveRefreshToken(email, refreshToken);
                // Refresh Token을 쿠키에 저장하기 위한 Cookie 객체 생성
                Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
                refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setHttpOnly(true);
                //refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
                return LoginResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshTokenCookie(refreshTokenCookie)
                        .build();
            }else {
                throw new BadCredentialsException("Invalid password");
            }
        }catch (UsernameNotFoundException unfe){
            throw new UsernameNotFoundException("해당 유저를 찾을 수 없습니다. "+email);
        }
    }
    //로그아웃 - jwt 처리 + refresh token 수정 서버에서 리프레시 토큰 삭제, 클라이언트에서 리프레시 쿠키 무효화
    public void setLogoutCookie(String email, HttpServletResponse response) {
        // Refresh Token Redis에서 삭제
        refreshTokenService.deleteRefreshToken(email);

        // Refresh Token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);
    }

    // 토큰 갱신 : 새로운 액세스 토큰을 응답 본문에 포함하고 새 리프레시 토큰 생성
    public LoginResponseDto refreshToken(String email, String refreshToken) {
        String storedRefreshToken = refreshTokenService.getRefreshToken(email);
        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
            String newAccessToken = jwtUtil.generateAccessToken(email);
            String newRefreshToken = jwtUtil.generateRefreshToken(email);

            // 새로운 Refresh 토큰을 Redis에 저장
            refreshTokenService.saveRefreshToken(email, newRefreshToken);

            // 새로운 Refresh Token을 쿠키에 저장하기 위한 Cookie 객체 생성
            Cookie refreshTokenCookie = new Cookie("refresh_token", newRefreshToken);
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);

            return LoginResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshTokenCookie(refreshTokenCookie)
                    .build();
        } else {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }
    //유저 닉네임 찾기
    public String findNicknameByEmail(String email){
    Users user =
        usersRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
        return user.getNickname();
    }
    //마이페이지 - 개인정보 수정
    @Transactional
    public UserDto modifyUserInfo(String email, ModifyUserInfoRequest modifyUserInfoReq) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Users existingUser = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 새 비밀번호가 제공된 경우에만 수정
        String newPassword = modifyUserInfoReq.getNewPassword();
        if (newPassword != null && !newPassword.isEmpty()) {
            // 비밀번호 암호화
            newPassword = passwordEncoder.encode(newPassword);
        } else {
            // 새 비밀번호가 제공되지 않았다면 기존 비밀번호 유지
            newPassword = existingUser.getPassword();
        }

        // 새 닉네임이 제공된 경우에만 수정
        String newNickname = modifyUserInfoReq.getNickname();
        if (newNickname == null || newNickname.isEmpty()) {
            newNickname = existingUser.getNickname();
        }

        existingUser.updateUserInfo(newPassword, newNickname);
        Users updatedUser = usersRepository.save(existingUser);
        return UserDto.fromEntity(updatedUser);
    }
    //비밀번호 초기화
    public String resetPassword(String email) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Users existingUsers = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String tempPassword = generateTempPasswword();
        existingUsers.resetPassword(passwordEncoder.encode(tempPassword));
        usersRepository.save(existingUsers);
        return tempPassword;
    }

    public String generateTempPasswword() {
        int codeLength = 8;  // 코드자리 6자리로 설정
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder sb = new StringBuilder(codeLength);
        Random random = new Random();

        for (int i = 0; i < codeLength; i++) {
            int index = random.nextInt(chars.length());
            char randomChar = chars.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }
    //회원 탈퇴 : 상태(status) ->2
    @Transactional
    public boolean withDrawalUser(String email, String password, HttpServletResponse response) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        try{
            Users existingUser = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            if (passwordEncoder.matches(password, existingUser.getPassword())) {
                existingUser.withDrawalUser(); //상태 0 -> 2
                usersRepository.save(existingUser);
                // Refresh Token Redis에서 삭제
                refreshTokenService.deleteRefreshToken(email);

                // Refresh Token 쿠키 삭제
                Cookie refreshTokenCookie = new Cookie("refresh_token", null);
                refreshTokenCookie.setMaxAge(0);
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setHttpOnly(true);
                response.addCookie(refreshTokenCookie);
                return true;
            }else {
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            }
        }catch (UsernameNotFoundException unfe) {
            log.error("회원 탈퇴 중 사용자를 찾을 수 없음: {}", email);
            throw unfe;
        } catch (Exception e) {
            log.error("회원 탈퇴 중 예외 발생", e);
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다.", e);
        }
    }
}