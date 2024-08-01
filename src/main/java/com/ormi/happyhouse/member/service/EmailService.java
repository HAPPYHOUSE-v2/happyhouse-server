package com.ormi.happyhouse.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    // Redis를 사용하여 인증코드 저장
    //private final RedisTemplate<String, String> redisTemplate;
    //private final @Qualifier("stringRedisTemplate") StringRedisTemplate redisTemplate;
    private final  StringRedisTemplate redisTemplate;

    //이메일 인증 코드 전송
    public void sendVerificationEmail(String recipient) {
        String emailCode = generateEmailCode();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient); //수신자 설정
        message.setSubject("이메일 인증"); //제목 설정
        message.setText("귀하의 인증 코드는 " + emailCode + " 입니다."); //내용 설정
        javaMailSender.send(message); //메일 전송

        // Redis에 인증코드 저장 (5분 동안 유효)
        redisTemplate.opsForValue().set("EMAIL_CODE:" + recipient, emailCode, 5, TimeUnit.MINUTES);
    }

    //이메일 인증코드 생성
    public String generateEmailCode() {
        int codeLength = 6;  // 코드자리 6자리로 설정
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(codeLength);
        Random random = new Random();

        for (int i = 0; i < codeLength; i++) {
            int index = random.nextInt(chars.length());
            char randomChar = chars.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }
    //이메일 인증코드 검증
    public boolean verifyEmailCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get("EMAIL_CODE:" + email);
        log.info("저장되어 있는 이메일과 인증코드 {}: {}", email, storedCode);
        log.info("받은 인증코드 : {}", code);
        if (storedCode != null && storedCode.equals(code)) {
            // 인증 성공 시 이메일 인증 상태를 Redis에 저장
            redisTemplate.opsForValue().set("EMAIL_VERIFIED:" + email, "true", 30, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    //임시 비밀번호 전송
    public void sendResetPassword(String to, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("비밀번호 초기화");
        message.setText("임시 비밀번호: " + tempPassword + "\n로그인 후 비밀번호를 변경해주세요.");
        javaMailSender.send(message);
    }
}
