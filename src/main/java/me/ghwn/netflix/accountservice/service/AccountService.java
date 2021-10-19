package me.ghwn.netflix.accountservice.service;

import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.dto.AccountCreationRequest;
import me.ghwn.netflix.accountservice.dto.AccountUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {

    AccountDto createAccount(AccountCreationRequest request);

    AccountDto getAccountDetail(Long id);

    Page<AccountDto> getAccountList(Pageable pageable);

    AccountDto updateAccount(Long id, AccountUpdateRequest request);

    void deleteAccount(Long id);
}
