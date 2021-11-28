package me.ghwn.netflix.accountservice.service;

import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
import me.ghwn.netflix.accountservice.dto.AccountUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AccountService extends UserDetailsService {

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    AccountDto createAccount(SignupRequest request);

    AccountDto getAccountById(Long id);

    AccountDto getAccountByAccountId(String accountId);

    AccountDto getAccountByEmail(String email);

    Page<AccountDto> getAccountList(Pageable pageable);

    AccountDto updateAccount(Long id, AccountUpdateRequest request);

    void deleteAccount(Long id);
}
