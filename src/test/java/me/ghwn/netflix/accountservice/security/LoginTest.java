package me.ghwn.netflix.accountservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.dto.LoginRequest;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import me.ghwn.netflix.accountservice.service.AccountService;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
public class LoginTest {

    // This access token has email ghwn5936@gmail.com
    private static final String EXPIRED_ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJlbWFpbCI6Imdod241OTM2QGdtYWlsLmNvbSIsImlhdCI6MTYzNzQ5MDU2OSwiZXhwIjoxNjM3NDk0MTY5fQ.YciAJlqN4B0vcNKLEDycCKKe3WaYecFsk8qwRC_ItFJw2koLAPKNrq5WTvzd4px0LrW6yR6lJcmZOxBPORJe_w";

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

    @DisplayName("Issue new JWT access token and refresh token")
    @Test
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
                .andExpect(header().exists("access-token"))
                .andExpect(header().exists("refresh-token"));
    }

    @DisplayName("Get account detail without login")
    @Test
    void accessTokenNotFound() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        String accountDetailUrl = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andReturn().getResponse().getRedirectedUrl();

        mockMvc.perform(get(accountDetailUrl))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Get account detail with login")
    @Test
    void useAccessToken() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        String accountDetailUrl = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andReturn().getResponse().getRedirectedUrl();

        // Login
        LoginRequest loginRequest = modelMapper.map(signupRequest, LoginRequest.class);
        MockHttpServletResponse response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists("access-token"))
                .andReturn().getResponse();

        // Get account detail
        String accessToken = response.getHeader("access-token");
        mockMvc.perform(get(accountDetailUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @DisplayName("Get account detail with invalid access token")
    @Test
    void useInvalidAccessToken() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        String accountDetailUrl = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andReturn().getResponse().getRedirectedUrl();

        // Login
        LoginRequest loginRequest = modelMapper.map(signupRequest, LoginRequest.class);
        MockHttpServletResponse response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists("access-token"))
                .andReturn().getResponse();

        // Get account detail
        String accessToken = response.getHeader("access-token");
        mockMvc.perform(get(accountDetailUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken + "hi"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Get account detail with expired access token")
    @Test
    void useExpiredAccessToken() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        String accountDetailUrl = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andReturn().getResponse().getRedirectedUrl();

        // Login
        LoginRequest loginRequest = modelMapper.map(signupRequest, LoginRequest.class);
        MockHttpServletResponse response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists("access-token"))
                .andReturn().getResponse();

        // Get account detail
        mockMvc.perform(get(accountDetailUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + EXPIRED_ACCESS_TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Issue new access token by passing refresh token")
    @Test
    void reissueAccessToken() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "ghwn5936@gmail.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        String accountDetailUrl = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andReturn().getResponse().getRedirectedUrl();

        // Login
        LoginRequest loginRequest = modelMapper.map(signupRequest, LoginRequest.class);
        MockHttpServletResponse response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists("refresh-token"))
                .andReturn().getResponse();

        String refreshToken = response.getHeader("refresh-token");

        // Get account detail
        mockMvc.perform(get(accountDetailUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + EXPIRED_ACCESS_TOKEN)
                        .header("refresh-token", refreshToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("access-token"));
    }

    /**
     * Server does not always issue new access token whenever valid refresh token is passed.
     * Server requires expired access token as well as the refresh token because the server compares refresh token
     * in the request header and the one stored in database.
     * The latter one can be obtained by passing email extracted from access token.
     * So even if a hacker steals a valid refresh token, he can't issue new access token unless he has also expired
     * access token that contains email of the refresh token's owner.
     */
    @DisplayName("Try to issue new access token by passing refresh token and invalid access token")
    @Test
    void reissueAccessTokenWithInvalidAccessToken() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        String accountDetailUrl = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andReturn().getResponse().getRedirectedUrl();

        // Login
        LoginRequest loginRequest = modelMapper.map(signupRequest, LoginRequest.class);
        MockHttpServletResponse response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists("refresh-token"))
                .andReturn().getResponse();

        String refreshToken = response.getHeader("refresh-token");

        // Get account detail
        mockMvc.perform(get(accountDetailUrl)
                        // Pass expired access token that contains different email.
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + EXPIRED_ACCESS_TOKEN)
                        .header("refresh-token", refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("access-token"));
    }

    @DisplayName("Get refresh token by calling API")
    @Test
    void getRefreshToken() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        AccountDto account = accountService.createAccount(signupRequest);

        // Login
        LoginRequest loginRequest = modelMapper.map(signupRequest, LoginRequest.class);
        MockHttpServletResponse response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists("access-token"))
                .andExpect(header().exists("refresh-token"))
                .andReturn().getResponse();
        String refreshToken = response.getHeader("refresh-token");

        // Query refresh token
        mockMvc.perform(get("/api/v1/accounts/{accountId}/refresh-token", account.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("value").value(refreshToken));
    }

    @DisplayName("Try to get empty refresh token")
    @Test
    void getRefreshTokenEmpty() throws Exception {
        // Sign up
        SignupRequest signupRequest = new SignupRequest(
                "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER.name()));
        AccountDto account = accountService.createAccount(signupRequest);

        // Query refresh token
        mockMvc.perform(get("/api/v1/accounts/{accountId}/refresh-token", account.getAccountId()))
                .andExpect(status().isNotFound());
    }
}
