package com.ormi.happyhouse.member.dto;

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
}
