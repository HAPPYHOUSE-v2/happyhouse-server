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
}
/*
public class Users implements UserDetails { //UserDetails를 상속 받아서 인증 객체로 사용함
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

    @Override //권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(UserRole.USER.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.nickname;
    }
    //계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        return true; // 만료되지 않음
        //return UserDetails.super.isAccountNonExpired();
    }
    //계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        return true;//잠금되지 않음
        //return UserDetails.super.isAccountNonLocked();
    }
    //비밀번호 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        return true;//만료되지 않음
        //return UserDetails.super.isCredentialsNonExpired();
    }
    //계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        return true;//사용 가능
        //return UserDetails.super.isEnabled();
    }
}
*/