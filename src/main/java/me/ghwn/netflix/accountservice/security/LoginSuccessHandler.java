package me.ghwn.netflix.accountservice.security;

import io.jsonwebtoken.Header;
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

    private static String ACCOUNT_ID_PAYLOAD_KEY = "aid";
    private static String ACCESS_TOKEN_HEADER_NAME = "access-token";
    private static String ACCOUNT_ID_HEADER_NAME = "account-id";

    private final String secret;
    private final Long accessExpirationTime;

    public LoginSuccessHandler(String secret, Long accessExpirationTime) {
        this.secret = secret;
        this.accessExpirationTime = accessExpirationTime;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        String accountId = accountContext.getAccount().getAccountId();
        String accessToken = createAccessToken(accountId, secret, accessExpirationTime);

        response.addHeader(ACCESS_TOKEN_HEADER_NAME, accessToken);
        response.addHeader(ACCOUNT_ID_HEADER_NAME, accountId);
    }

    private String createAccessToken(String accountId, String secret, Long accessExpirationTime) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, "JWT")
                .claim(ACCOUNT_ID_PAYLOAD_KEY, accountId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (accessExpirationTime * 1000)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

}
