package me.ghwn.netflix.accountservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import me.ghwn.netflix.accountservice.service.AccountService;
import me.ghwn.netflix.accountservice.service.JsonWebTokenService;
import org.apache.http.HttpHeaders;
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

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String EMAIL_PAYLOAD_FIELD = "email";

    private final String secret;
    private final AccountService accountService;
    private final JsonWebTokenService jsonWebTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Extract authorization header
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Valid JWT access token
        String accessToken = authorization.substring(BEARER_TOKEN_PREFIX.length());
        String email;
        try {
            Claims accessClaims = jsonWebTokenService.parseAccessToken(accessToken, secret);
            email = accessClaims.get(EMAIL_PAYLOAD_FIELD, String.class);
        } catch (JwtException e) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get user identity and set it on the spring security context
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
}
