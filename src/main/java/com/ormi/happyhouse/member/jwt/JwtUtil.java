package com.ormi.happyhouse.member.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String secret;

    @Value("${jwt.access-token-expiration-time}")
    private long accessTokenValidity;

    //Refresh Token
    @Value("${jwt.refresh-token-expiration-time}")
    private long refreshTokenValidity;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }
    private String generateToken(String email, long validity, String tokenType) {
        Date now = new Date();
        Date expDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .subject(email)
                .claim("tokenType", tokenType)
                .issuedAt(now)
                .expiration(expDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS256)
                .compact();
    }

    public String generateAccessToken(String email) {
        return generateToken(email, accessTokenValidity, "ACCESS");
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, refreshTokenValidity, "REFRESH");
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException exje) {
            return false; // 토큰 만료
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
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
/*
    public String generateToken(String email, long validity){
        Date now = new Date();
        Date expDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS256)
                .compact();
    }
    //엑세스 토큰
    public String generateAccessToken(String email) {
        return generateToken(email, accessTokenValidity);
    }
    //리프레시 토큰
    public String generateRefreshToken(String email) {
        return generateToken(email, refreshTokenValidity);
    }
    // JWT 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {

            //토큰 서명 검증(올바른 키 사용 여부) / 토큰 구조 검증
            //Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
            //return true;

            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            //만료 시간 검사
            return !claims.getExpiration().before(new Date());
        }catch (ExpiredJwtException exje){
            return false; // 토큰 만료
        }catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
*/
}
