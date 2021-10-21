package me.ghwn.netflix.accountservice.security;

import lombok.RequiredArgsConstructor;
import me.ghwn.netflix.accountservice.filter.EmailPasswordAuthenticationFilter;
import me.ghwn.netflix.accountservice.service.AccountService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

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
                .anyRequest().authenticated()

                .and()
                .addFilter(new EmailPasswordAuthenticationFilter(authenticationManager()));
    }
}
