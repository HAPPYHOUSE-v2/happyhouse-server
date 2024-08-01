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
    private int status; // 0
    //private Provider provider; //Local
    //private LocalDateTime createdAt;

    public static UserDto fromEntity(Users user) {
        return UserDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
