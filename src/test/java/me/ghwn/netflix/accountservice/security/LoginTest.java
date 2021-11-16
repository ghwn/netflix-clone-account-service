package me.ghwn.netflix.accountservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
import me.ghwn.netflix.accountservice.dto.LoginRequest;
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
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class LoginTest {

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

    @Test
    @DisplayName("Issue new JWT access token")
    void issueJwtToken() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        accountService.createAccount(signupRequest);

        // Login
        LoginRequest loginRequest = modelMapper.map(signupRequest, LoginRequest.class);
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists("access-token"));
    }
}
