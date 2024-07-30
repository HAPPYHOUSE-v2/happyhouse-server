package com.ormi.happyhouse.member.controller;

import com.ormi.happyhouse.member.dto.UserDto;
import com.ormi.happyhouse.member.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void testLoginEndpoint() throws Exception {
        // 테스트용 사용자 생성
        UserDto testUser = new UserDto();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setNickname("TestUser");
        userService.register(testUser);

        // 로그인 요청 수행
        mockMvc.perform(post("/member/login")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().exists("jwt_token"))
                .andExpect(cookie().maxAge("jwt_token", 600));
    }
}
