package com.ormi.happyhouse.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


//Redis를 사용하여 JWT Refresh 토큰을 저장하고 관리하는 서비스
@Slf4j
@Service
public class RefreshTokenService {
    //private final RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate redisTemplate;
    private final Duration refreshTokenValidityDuration;
    /*
    public RefreshTokenService(RedisTemplate<String, String> redisTemplate,
                               @Value("${jwt.refresh-token-expiration-time}") long refreshTokenValidity){
        this.redisTemplate = redisTemplate;
        //주입받은 유효 기간을 Duration 객체로 변환하여 저장
        this.refreshTokenValidityDuration = Duration.ofMillis(refreshTokenValidity);
    }
    */
    public RefreshTokenService(StringRedisTemplate redisTemplate,
                               @Value("${jwt.refresh-token-expiration-time}") long refreshTokenValidity){
        this.redisTemplate = redisTemplate;
        //주입받은 유효 기간을 Duration 객체로 변환하여 저장
        this.refreshTokenValidityDuration = Duration.ofMillis(refreshTokenValidity);
    }
    /*
    사용자의 Refresh Token을 Redis에 저장
    getKey()를 사용하여 사용자별 고유 키 생성
    redisTemplate.opsForValue().set()으로 키-값 쌍 저장(저장 시 토큰의 유효기간도 함께 설정)
    */
    public void saveRefreshToken(String username, String refreshToken){
        redisTemplate.opsForValue().set(getKey(username), refreshToken, refreshTokenValidityDuration);
    }

    //주어진 사용자명에 해당하는 refresh token을 redis에서 조회
    //토큰이 없거나 만료된 경우 null 반환
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(getKey(username));
    }

    //토큰을 redis에서 삭제
    public void deleteRefreshToken(String username) {
        redisTemplate.delete(getKey(username));
    }

    private String getKey(String username) {
        return "refreshToken:" + username;
    }
}
