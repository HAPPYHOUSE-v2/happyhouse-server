package com.ormi.happyhouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @Getter
@AllArgsConstructor
@NoArgsConstructor
public class WithDrawalRequest {
    private String password;
}
