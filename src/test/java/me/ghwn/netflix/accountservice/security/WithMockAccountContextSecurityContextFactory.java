package me.ghwn.netflix.accountservice.security;

import me.ghwn.netflix.accountservice.entity.Account;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class WithMockAccountContextSecurityContextFactory implements WithSecurityContextFactory<WithMockAccountContext> {

    @Override
    public SecurityContext createSecurityContext(WithMockAccountContext withMockAccountContext) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Set<AccountRole> accountRoles = Arrays.stream(withMockAccountContext.roles())
                .map(role -> AccountRole.valueOf(role.toUpperCase()))
                .collect(Collectors.toSet());
        Account account = new Account(
                null,
                UUID.randomUUID().toString(),
                withMockAccountContext.email(),
                withMockAccountContext.password(),
                true,
                accountRoles
        );
        Set<SimpleGrantedAuthority> authorities = accountRoles.stream()
                .map(accountRole -> new SimpleGrantedAuthority("ROLE_" + accountRole.name()))
                .collect(Collectors.toSet());
        AccountContext accountContext = new AccountContext(account, authorities);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                accountContext,
                null,
                authorities
        );
        securityContext.setAuthentication(authenticationToken);
        return securityContext;
    }
}
