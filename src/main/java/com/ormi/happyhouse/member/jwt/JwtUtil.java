package com.ormi.happyhouse.member.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.SignatureException;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
  @Value("${jwt.secret-key}")
  private String secret;

  @Value("${jwt.access-token-expiration-time}")
  private long accessTokenValidity;

  // Refresh Token
  @Value("${jwt.refresh-token-expiration-time}")
  private long refreshTokenValidity;

  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  private String generateToken(String email, long validity, String tokenType, String role) {
    Date now = new Date();
    Date expDate = new Date(now.getTime() + validity);

    return Jwts.builder()
        .subject(email)
        .claim("role", role) // 권한
        .claim("tokenType", tokenType)
        .issuedAt(now)
        .expiration(expDate)
        .signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS256)
        .compact();
  }

  public String generateAccessToken(String email, String role) { //권한 추가
    return generateToken(email, accessTokenValidity, "ACCESS", role);
  }

  public String generateRefreshToken(String email, String role) { //권한 추가
    return generateToken(email, refreshTokenValidity, "REFRESH", role);
  }

  // 토큰 검증
  public boolean validateToken(String token) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith((SecretKey) key) // 비밀 키 설정
              .build()
              .parseSignedClaims(token) // JWT 파싱 및 건증
              .getPayload();
      return !claims.getExpiration().before(new Date());
      // } catch (SignatureException e) {
      //    log.error("토큰 서명 오류: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("잘못된 토큰 형식: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("토큰 만료: {}", e.getMessage());
      throw e;
    } catch (UnsupportedJwtException e) {
      log.error("지원하지 않는 토큰: {}", e.getMessage());
    } catch (JwtException e) {
      log.error("토큰 검증 오류: {}", e.getMessage());
      throw e;
    }
    return false;
  }

  public String getEmailFromToken(String token) {
    return Jwts.parser()
        .verifyWith((SecretKey) key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public String getTokenType(String token) {
    return Jwts.parser()
        .verifyWith((SecretKey) key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("tokenType", String.class);
  }

  // 토큰에서 권한 정보 가져오기
  public String getRoleFromToken(String token) {
    return Jwts.parser()
        .verifyWith((SecretKey) key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("role", String.class);
  }
}
