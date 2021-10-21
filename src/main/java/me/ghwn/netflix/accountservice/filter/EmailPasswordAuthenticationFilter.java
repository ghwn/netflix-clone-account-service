package me.ghwn.netflix.accountservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ghwn.netflix.accountservice.dto.AccountLoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EmailPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public EmailPasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String email, password;
        try {
            AccountLoginRequest accountLoginRequest = new ObjectMapper()
                    .readValue(request.getInputStream(), AccountLoginRequest.class);
            email = accountLoginRequest.getEmail();
            email = (email != null) ? email.trim() : "";
            password = accountLoginRequest.getPassword();
            password = (password != null) ? password : "";
        } catch (IOException e) {
            email = "";
            password = "";
        }
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, password);
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        // Do nothing
    }
}
