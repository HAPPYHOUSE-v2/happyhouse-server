package com.ormi.happyhouse.member.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Users {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  @Column(nullable = false, unique = true)
  private String email;

  private String password;

  @Column(nullable = false, unique = true, length = 20)
  private String nickname;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column(nullable = false, columnDefinition = "int default 0")
  private int status;

  @Enumerated(EnumType.STRING)
  private Provider provider;

  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  //개인정보 수정
  public void updateUserInfo(String password, String nickname){
    this.password = password;
    this.nickname = nickname;
  }
  //비밀번호 초기화
  public void resetPassword(String newPassword){
    this.password = newPassword;
  }
  //회원 탈퇴
  public void withDrawalUser(){
    this.status = 2;
  }
}