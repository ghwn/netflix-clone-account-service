package me.ghwn.netflix.accountservice.service;

import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.vo.AccountCreationRequest;

import java.util.Optional;

public interface AccountService {

    AccountDto createAccount(AccountCreationRequest request);

    Optional<AccountDto> getAccountDetail(Long id);
}
