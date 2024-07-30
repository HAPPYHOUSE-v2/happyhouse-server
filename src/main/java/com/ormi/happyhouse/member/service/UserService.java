package com.ormi.happyhouse.member.service;

import com.ormi.happyhouse.member.dto.LoginResponseDto;
import com.ormi.happyhouse.member.exception.InvalidRefreshTokenException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

//Spring Security에서 사용자 정보를 가져오는 인터페이스
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UsersRepository usersRepository;
    //private final PasswordEncoder passwordEncoder; //순환참조
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // 회원가입
    @Transactional
    public void register(UserDto userDto) {
      // 이메일 검사
      if (!StringUtils.hasText(userDto.getEmail())) {
        throw new UserRegistrationException("이메일은 필수 입력 항목입니다.");
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
    //public Cookie loginWithJwt(String email, String password){
    public LoginResponseDto loginWithJwt(String email, String password){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        try{
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
                /*
                //Access Token을 쿠키에 저장
                Cookie jwtCookie = new Cookie("jwt_token", accessToken);
                jwtCookie.setMaxAge(600);
                jwtCookie.setPath("/");
                jwtCookie.setHttpOnly(true);
                //return jwtCookie;
                return LoginResponseDto.builder()
                        .accessTokenCookie(jwtCookie)
                        .refreshToken(refreshToken).build();
                */
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

        // 클라이언트에게 Access Token 삭제 지시
        // (실제 삭제는 클라이언트 측에서 수행)
    }
    /*public Cookie setLogoutCookie(String email) {
        //Refresh Token Redis에서 삭제
        refreshTokenService.deleteRefreshToken(email);
        //Access Token 무효화
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        return jwtCookie;
    }*/

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
    /*public LoginResponseDto refreshToken(String email, String refreshToken) {
        String storedRefreshToken = refreshTokenService.getRefreshToken(email);
        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
            String newAccessToken = jwtUtil.generateAccessToken(email);
            String newRefreshToken = jwtUtil.generateRefreshToken(email);

            // 새로운 Refresh 토큰을 Redis에 저장
            refreshTokenService.saveRefreshToken(email, newRefreshToken);

            // 새로운 AccessToken을 쿠키에 저장
            Cookie jwtCookie = new Cookie("jwt_token", newAccessToken);
            jwtCookie.setMaxAge(600);
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true);

            return LoginResponseDto.builder()
                    .accessTokenCookie(jwtCookie)
                    .refreshToken(refreshToken).build();
        } else {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }*/
}