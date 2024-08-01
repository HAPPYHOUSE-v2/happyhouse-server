package com.ormi.happyhouse.member.dto;

import com.ormi.happyhouse.member.domain.Users;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String email;
    private String password;
    private String nickname;
    //private UserRole role; // User
    //private int status; // 0
    //private Provider provider; //Local
    //private LocalDateTime createdAt;
    /*
    //팩토리 메서드 추가
   public static UserDto of(String email, String password, String nickname) {
        return new UserDto(email, password, nickname);
    }*/
    // Users 엔티티를 UserDto로 변환하는 정적 메소드
    public static UserDto fromEntity(Users user) {
        return UserDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
