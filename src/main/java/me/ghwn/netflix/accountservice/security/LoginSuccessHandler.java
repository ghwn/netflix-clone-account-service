package me.ghwn.netflix.accountservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ISSUED_TOKEN_HEADER_NAME = "X-Auth-Token";

    private final String secret;
    private final Long expiresInSeconds;

    public LoginSuccessHandler(String secret, Long expiresInSeconds) {
        this.secret = secret;
        this.expiresInSeconds = expiresInSeconds;
    }

    // FIXME: Add refresh token
    /**
     * Issue new JWT token and add it into response header.
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
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        String subject = accountContext.getAccount().getEmail();

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);

        String jwtToken = Jwts.builder()
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + (expiresInSeconds * 1000)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        response.addHeader(ISSUED_TOKEN_HEADER_NAME, jwtToken);
    }
}
