package com.ormi.happyhouse.member.config;

import com.ormi.happyhouse.member.jwt.JwtFilter;
import com.ormi.happyhouse.member.jwt.JwtUtil;
import com.ormi.happyhouse.member.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity //Spring 설정 클래스, Spring Security 활성화
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    //Spring Security 필터 체인 구성
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/member/register", "/member/login",
                                "/member/refresh", "/member/duplicateNickname",
                                "/member/send-verification-email", "/member/verify-email", "/member/logout").permitAll()
                        .requestMatchers("/static/**", "/webjars/**", "/css/**", "/js/**", "/image/**").permitAll()
                        .requestMatchers( "/member/check-auth", "/member/mypage").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(userService, jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                        })
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8089")); // 프론트엔드 주소
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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