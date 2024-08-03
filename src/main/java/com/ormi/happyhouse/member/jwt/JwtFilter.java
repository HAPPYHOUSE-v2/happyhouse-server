package com.ormi.happyhouse.member.jwt;

import com.ormi.happyhouse.member.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

//JWT를 사용한 인증을 처리하는 security 필터
//OncePerRequestFilter를 상속 받아 모든 요청에 대해 한번씩 실행함
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    //모든 HTTP 요청에 대해 실행되며 쿠키에서 JWT 토큰을 추출하여 토큰이 유효한 경우 사용자 정보를 로드하고 인증 객체 생성
    //액세스 토큰은 Authorization 헤더에서 추출 / 리프레시 토큰은 쿠키에서 추출
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                                    throws ServletException, IOException {
        // 로그아웃 요청인 경우 토큰 검증 수행 skip (사용자 토큰이 이미 만료되었을 경우, 토큰검증 시 로그아웃 불가)
        if (isLogoutRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 공개 접근 경로에 대해서는 필터 처리 skip( 공개된 페이지는 토큰 인증 X)
        if (isPublicPath(request.getRequestURI(), request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String accessToken = extractAccessToken(request);
        String refreshToken = extractRefreshTokenFromCookie(request);

        try{
            if (accessToken != null && jwtUtil.validateToken(accessToken)) {
                processToken(accessToken, request);
            } else if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
                // Access Token이 유효하지 않고 Refresh Token이 유효한 경우
                // 새로운 Access Token을 발급하고 응답 헤더에 추가
                String email = jwtUtil.getEmailFromToken(refreshToken);
                String role = jwtUtil.getRoleFromToken(refreshToken); //권한 추가
                String newAccessToken = jwtUtil.generateAccessToken(email, role); //권한 추가
                response.setHeader("Authorization", "Bearer " + newAccessToken);
                processToken(newAccessToken, request);
            }else{ //액세스 토큰 없거나 리프레시 토큰 유효하지 않은 경우 401
                handleAuthenticationException(response, "인증이 필요");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (AccessDeniedException e) {
            response.sendRedirect("/error/403");

        } catch (ExpiredJwtException e) {
            // 토큰 만료 예외 처리
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token expired\", \"shouldRefresh\": true}");
            return;
        } catch (Exception e){
            //response.sendRedirect("/error/401");
            handleAuthenticationException(response, "인증 처리 중 오류 발생");
        }
    }
    private void handleTokenExpiration(HttpServletResponse response, String refreshToken) throws IOException {
        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            // 리프레시 토큰으로 새 액세스 토큰 발급
            String email = jwtUtil.getEmailFromToken(refreshToken);
            String role = jwtUtil.getRoleFromToken(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(email, role);
            response.setHeader("Authorization", "Bearer " + newAccessToken);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\": \"New access token issued\"}");
        } else {
            // 리프레시 토큰도 만료된 경우
            handleAuthenticationException(response, "세션이 만료되었습니다. 다시 로그인해 주세요.");
        }
    }
    //인증 예외 처리
    private void handleAuthenticationException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private void processToken(String token, HttpServletRequest request) throws AccessDeniedException {
        String email = jwtUtil.getEmailFromToken(token);
        String role = jwtUtil.getRoleFromToken(token); // 권한
        UserDetails userDetails = userService.loadUserByUsername(email);
        //UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        //        userDetails, null, userDetails.getAuthorities());
        //권한
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 관리자 페이지에 대한 접근 권한 검사
        if (request.getRequestURI().startsWith("/admin") && !role.equals("ADMIN")) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    // 공개 접근 경로인지 확인하는 메소드
    private boolean isPublicPath(String path, String method) {
        if(path.equals("/post") && method.equalsIgnoreCase("GET")){
            return true;
        }
        //로고 이미지 등 모든 유저 볼 수 있게 처리(없으면 로그아웃일 때 401에러)
        if (path.startsWith("/image/") || path.startsWith("/static/")) {
            return true;
        }
        return path.equals("/member/register") ||
                path.equals("/member/login") ||
                path.equals("/member/duplicateNickname") ||
                path.equals("/member/send-verification-email") ||
                path.equals("/")||
                path.equals("/member/verify-email") ||
                path.equals("/member/temppassword");
    }
    //로그아웃 요청 여부 확인
    private boolean isLogoutRequest(HttpServletRequest request) {
        return request.getMethod().equals("POST") && request.getRequestURI().equals("/member/logout");
    }
    //요청에서 액세스 토큰 추출
    private String extractAccessToken(HttpServletRequest req){
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    //요청에서 쿠키의 리프레시 토큰 추출
    private String extractRefreshTokenFromCookie(HttpServletRequest req){
        Cookie[] cookies = req.getCookies();
        if(cookies!=null){
            for(Cookie c: cookies){
                if("refresh_token".equals(c.getName())){
                    return c.getValue();
                }
            }
        }
        return null;
    }
}