package me.ghwn.netflix.accountservice.security;

import me.ghwn.netflix.accountservice.service.JsonWebTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ACCESS_TOKEN_HEADER_NAME = "access-token";
    private static final String REFRESH_TOKEN_HEADER_NAME = "refresh-token";

    private final JsonWebTokenService jsonWebTokenService;
    private final String secret;
    private final Long accessExpirationTime;
    private final Long refreshExpirationTime;

    public LoginSuccessHandler(JsonWebTokenService jsonWebTokenService,
                               String secret,
                               Long accessExpirationTime,
                               Long refreshExpirationTime) {
        this.jsonWebTokenService = jsonWebTokenService;
        this.secret = secret;
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
    }


    /**
     * Issues new JWT access token and add it into the response header.
     * FIXME: Add refresh token
     *
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        User user = (User) authentication.getPrincipal();
        String email = user.getUsername();
        String accessToken = jsonWebTokenService.createAccessToken(email, secret, accessExpirationTime);
        String refreshToken = jsonWebTokenService.createRefreshToken(email, secret, refreshExpirationTime);
        response.addHeader(ACCESS_TOKEN_HEADER_NAME, accessToken);
        response.addHeader(REFRESH_TOKEN_HEADER_NAME, refreshToken);
    }
}
