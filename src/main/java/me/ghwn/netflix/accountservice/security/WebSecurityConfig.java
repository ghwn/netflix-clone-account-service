package me.ghwn.netflix.accountservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ghwn.netflix.accountservice.service.AccountService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.Filter;
import java.util.Objects;

@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService).passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .antMatchers("/docs/index.html");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // FIXME: Not recommended to disable CSRF
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/v1/accounts", "/login").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()

                .and()
                .addFilter(buildAuthenticationFilter());
    }

    /**
     * Build a custom authentication filter.
     * @return filter
     * @throws Exception
     */
    private Filter buildAuthenticationFilter() throws Exception {
        LoginFilter filter = new LoginFilter(authenticationManager());
        String secret = Objects.requireNonNull(env.getProperty("jwt.secret"));
        Long expiresInSeconds = Long.parseLong(Objects.requireNonNull(env.getProperty("jwt.expires-in-seconds")));
        filter.setAuthenticationSuccessHandler(new LoginSuccessHandler(secret, expiresInSeconds));
        return filter;
    }
}