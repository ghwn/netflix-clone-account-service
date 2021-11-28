package me.ghwn.netflix.accountservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ghwn.netflix.accountservice.service.AccountService;
import me.ghwn.netflix.accountservice.service.JsonWebTokenService;
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
    private final JsonWebTokenService jsonWebTokenService;
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
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll()

                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .addFilter(buildLoginFilter())
                .addFilterBefore(buildAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Builds a custom login filter.
     *
     * @return LoginFilter
     * @throws Exception
     */
    private Filter buildLoginFilter() throws Exception {
        LoginFilter filter = new LoginFilter(authenticationManager());
        String secret = Objects.requireNonNull(env.getProperty("jwt.secret"));
        Long accessExpirationTime = Long.parseLong(Objects.requireNonNull(env.getProperty("jwt.access-token.expiration-time", "3600")));
        Long refreshExpirationTime = Long.parseLong(Objects.requireNonNull(env.getProperty("jwt.refresh-token.expiration-time", "1209600")));
        LoginSuccessHandler loginSuccessHandler = new LoginSuccessHandler(jsonWebTokenService, secret, accessExpirationTime, refreshExpirationTime);
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        return filter;
    }

    /**
     * Builds authentication filter.
     *
     * @return AuthenticationFilter
     * @throws Exception
     */
    private Filter buildAuthenticationFilter() throws Exception {
        return new AuthenticationFilter(accountService);
    }

}
