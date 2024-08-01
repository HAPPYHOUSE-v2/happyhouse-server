package com.ormi.happyhouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModifyUserInfoRequest {
    private String nickname;
    private String newPassword;
}
