package com.ormi.happyhouse.member.config;

import com.ormi.happyhouse.member.jwt.JwtFilter;
import com.ormi.happyhouse.member.jwt.JwtUtil;
import com.ormi.happyhouse.member.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity //Spring 설정 클래스, Spring Security 활성화
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Bean //Spring Security 필터 체인 구성
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())  // CORS 설정 추가
                .csrf(csrf -> csrf.disable())
                //서버는 클라이언트의 세션 정보를 저장하지 않고, 매 요청마다 클라이언트가 제공하는 인증 정보를 기반으로 인증을 수행
                //STATLESS는 jwt과 어울림
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/member/register", "/member/login","/member/logout", "/member/refresh","/member/duplicateNickname", "/member/check-auth").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(userService, jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
        /*
        //http 요청 인증 설정
        http.authorizeHttpRequests((requests) -> requests
                        //.requestMatchers("/member/register", "/member/login", "/member/duplicateNickname").permitAll() //모든 사용자에게 허용(그 외 모든 요청은 인증된 사용자만 가능)
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
        */
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