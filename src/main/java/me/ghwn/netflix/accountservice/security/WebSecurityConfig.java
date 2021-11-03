package me.ghwn.netflix.accountservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ghwn.netflix.accountservice.service.AccountService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.Filter;
import java.util.Objects;

@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
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
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .requestMatchers(PathRequest.toH2Console())
                .antMatchers("/docs/index.html");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // FIXME: Not recommended to disable CSRF
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll()

                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .addFilter(buildLoginFilter())
                .addFilterBefore(buildJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Builds a custom login filter.
     * Login requires HTTP message body that contains 'email' and 'password' in JSON format.
     * If login is succeeded, a new JWT token is added into the response header.
     * For more details, see LoginFilter and LoginSuccessHandler.
     * @return filter
     * @throws Exception
     */
    private Filter buildLoginFilter() throws Exception {
        LoginFilter filter = new LoginFilter(authenticationManager());
        String secret = Objects.requireNonNull(env.getProperty("jwt.secret"));
        Long expiresInSeconds = Long.parseLong(Objects.requireNonNull(env.getProperty("jwt.expires-in-seconds", "3600")));
        filter.setAuthenticationSuccessHandler(new LoginSuccessHandler(secret, expiresInSeconds));
        return filter;
    }

    /**
     * Builds a custom JWT authentication filter.
     * JWT makes decentralized authentication possible, which means that all microservices should be able to parse JWT token.
     * @return filter
     */
    private Filter buildJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(env.getProperty("jwt.secret"), accountService);
    }
}
