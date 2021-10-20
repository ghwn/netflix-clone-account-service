package me.ghwn.netflix.accountservice.service;

import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.dto.AccountCreationRequest;
import me.ghwn.netflix.accountservice.dto.AccountUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AccountService extends UserDetailsService {

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    AccountDto createAccount(AccountCreationRequest request);

    AccountDto getAccountDetail(Long id);

    Page<AccountDto> getAccountList(Pageable pageable);

    AccountDto updateAccount(Long id, AccountUpdateRequest request);

    void deleteAccount(Long id);
}
