package com.ormi.happyhouse.member.config;

import com.ormi.happyhouse.member.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity //Spring 설정 클래스, Spring Security 활성화
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final UserService userService;

    /*public SpringSecurityConfig(UserDetailsService userService){
        this.userService = userService;
    }*/
    @Bean //Spring Security 필터 체인 구성
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //http 요청 인증 설정
        http.authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/member/register", "/member/login", "/member/duplicateNickname").permitAll() //모든 사용자에게 허용(그 외 모든 요청은 인증된 사용자만 가능)
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login") //로그인 요청 처리 URL
                        .usernameParameter("email")  // loadByUserName에서 email
                        .defaultSuccessUrl("/", true) // 로그인 성공 시 메인으로 리다이렉트
                        .failureUrl("/member/login?error")
                        .permitAll()
                ).logout((logout) -> logout
                        .logoutSuccessUrl("/") //로그아웃 성공 시 메인
                        .permitAll());

        return http.build();
    }
    @Bean //비밀번호 암호화 저장
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean //Spring Security에서 사용자 인증을 처리하는 AuthenticationProvider 설정
    public AuthenticationProvider authenticationProvider() {
    // DaoAuthenticationc        //비밀번호 인코더 설정. 저장된 비밀번호와 입력된 비밀번호를 안전하게 비교Provider :
    // AuthenticationProvider의 구현체 중 하나. DAO를 사용하여 사용자 정보 조회, 인증 수행
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        //UserDetailsService를 설정.사용자 이름(보통 이메일)을 기반으로 사용자 정보를 로드하는 역할
        authProvider.setUserDetailsService(userService);
        //비밀번호 인코더 설정. 저장된 비밀번호와 입력된 비밀번호를 안전하게 비교
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
