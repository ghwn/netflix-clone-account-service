package me.ghwn.netflix.accountservice.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import me.ghwn.netflix.accountservice.dto.RefreshTokenDto;
import me.ghwn.netflix.accountservice.service.AccountService;
import me.ghwn.netflix.accountservice.service.JsonWebTokenService;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String EMAIL_PAYLOAD_FIELD = "email";
    private static final String ACCESS_TOKEN_HEADER_NAME = "access-token";
    private static final String REFRESH_TOKEN_HEADER_NAME = "refresh-token";

    private final String secret;
    private final Long accessExpirationTime;
    private final AccountService accountService;
    private final JsonWebTokenService jsonWebTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Check if request contains Authorization header.
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            logger.error("Access token not found");
            sendError(response, HttpStatus.UNAUTHORIZED, "Access token not found");
            return;
        }

        // Check if Authorization header is valid.
        if (!authorization.startsWith(BEARER_TOKEN_PREFIX)) {
            logger.error("Access token not valid");
            sendError(response, HttpStatus.UNAUTHORIZED, "Access token not valid");
            return;
        }

        // Extract access token from Authorization header.
        String accessToken = authorization.substring(BEARER_TOKEN_PREFIX.length());

        // Check if access token is valid.
        String email = null;
        try {
            email = jsonWebTokenService.parseAccessToken(accessToken, secret)
                    .getBody()
                    .get(EMAIL_PAYLOAD_FIELD, String.class);
        } catch (ExpiredJwtException e) {
            // If access token is expired, then check if the request also contains refresh token.
            String refreshToken = request.getHeader(REFRESH_TOKEN_HEADER_NAME);
            if (refreshToken == null) {
                logger.error("refresh token not found");
                sendError(response, HttpStatus.UNAUTHORIZED, "Access token expired");
                return;
            }

            // Validate refresh token.
            try {
                jsonWebTokenService.parseRefreshToken(refreshToken, secret);
            } catch (JwtException e2) {
                e2.printStackTrace();
                sendError(response, HttpStatus.UNAUTHORIZED, "Invalid refresh token");
                return;
            }

            // Extra validation by comparing refresh token with the one stored in the database.
            // Extract email from access token.
            String email_ = e.getClaims().get(EMAIL_PAYLOAD_FIELD, String.class);
            if (email_ == null) {
                logger.error("Email not found in access token");
                sendError(response, HttpStatus.UNAUTHORIZED, "Invalid access token");
                return;
            }
            // Query refresh token by sending email.
            RefreshTokenDto refreshTokenDb = jsonWebTokenService.getRefreshToken(email_);
            if (refreshTokenDb == null || !refreshToken.equals(refreshTokenDb.getValue())) {
                logger.error("Refresh token does not match");
                sendError(response, HttpStatus.UNAUTHORIZED, "Invalid refresh token");
                return;
            }

            // Issue new access token and add it to response header.
            String newAccessToken = jsonWebTokenService.createAccessToken(email_, secret, accessExpirationTime);
            response.setHeader(ACCESS_TOKEN_HEADER_NAME, newAccessToken);
            email = email_;

        } catch (JwtException | NullPointerException e) {
            e.printStackTrace();
            sendError(response, HttpStatus.UNAUTHORIZED, "Invalid access token");
            return;
        }

        // Get user identity and set it on the spring security context
        if (email == null) {
            sendError(response, HttpStatus.UNAUTHORIZED, "Invalid access token");
            return;
        }
        UserDetails userDetails = accountService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails == null ? Collections.emptyList() : userDetails.getAuthorities()
        );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, HttpStatus httpStatus, String message) {
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        String body;
        try {
            body = objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            body = "";
        }
        try {
            response.getWriter().write(body);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        return method.equalsIgnoreCase("POST") && (uri.equals("/api/v1/accounts") || uri.equals("/login"));
    }
}
