package com.ormi.happyhouse.member.service;

import com.ormi.happyhouse.member.domain.Provider;
import com.ormi.happyhouse.member.domain.UserRole;
import com.ormi.happyhouse.member.domain.Users;
import com.ormi.happyhouse.member.dto.UserDto;
import com.ormi.happyhouse.member.exception.UserRegistrationException;
import com.ormi.happyhouse.member.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.security.auth.login.LoginException;
import java.util.Optional;
/*
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UsersRepository usersRepository;
  private final PasswordEncoder passwordEncoder;

  // 회원가입
  public void register(UserDto userDto) {
    // 이메일 유효성 검사
    if (!StringUtils.hasText(userDto.getEmail())) {
      throw new UserRegistrationException("이메일은 필수 입력 항목입니다.");
    }
    try {
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

  // 로그인 처리
  public Long loginUser(String email, String password) throws LoginException {
    Optional<Users> userOptional = usersRepository.findByEmail(email);

    if (userOptional.isPresent()) {
      Users user = userOptional.get();
      if(passwordEncoder.matches(password, user.getPassword())){
        log.info("로그인 성공 :{}", email);
        return user.getUserId();
      }else{
        log.error("로그인 실패-이메일 또는 비밀번호가 올바르지 않음 :{}", email);
        throw new LoginException("이메일 또는 비밀번호가 올바르지 않습니다.");
      }
    } else {
      log.warn("로그인 실패-유저 존재하지 않음 :{}", email);
      throw new LoginException("해당 유저가 존재하지 않습니다.");
    }
  }
}
*/

//Spring Security에서 사용자 정보를 가져오는 인터페이스
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UsersRepository usersRepository;
    //private final PasswordEncoder passwordEncoder; //순환참조
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

    @Override
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
    //닉네임 중복확인
    public boolean isDuplicatedNickname(String nickname) {
        return usersRepository.existsByNickname(nickname);
    }
}