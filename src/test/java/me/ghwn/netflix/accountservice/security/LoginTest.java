package me.ghwn.netflix.accountservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ghwn.netflix.accountservice.dto.LoginRequest;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import me.ghwn.netflix.accountservice.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
public class LoginTest {

    // This access token has accountId cab2795b-40f6-483d-9c51-4c3e401dad76
    private static final String EXPIRED_ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhaWQiOiJjYWIyNzk1Yi00MGY2LTQ4M2QtOWM1MS00YzNlNDAxZGFkNzYiLCJpYXQiOjE2MzgxMDY5NjAsImV4cCI6MTYzODEwNzAyMH0._vKOmwVqr6QqVh0HZmOGOYuUmIeR2AX1UZ5p_449DneNReW0Y6PyIoI_87c21x0SUAGhmoNI7epQ8NqopVIUMA";

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountService accountService;
    @Autowired ModelMapper modelMapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @DisplayName("Issue new JWT token")
    @Test
    void issueJwtToken() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER));
        accountService.createAccount(signupRequest);

        // Login
        LoginRequest loginRequest = modelMapper.map(signupRequest, LoginRequest.class);
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists("access-token"))
                .andExpect(header().exists("account-id"));
    }

}
