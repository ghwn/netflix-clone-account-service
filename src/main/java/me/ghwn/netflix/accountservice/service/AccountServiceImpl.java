package me.ghwn.netflix.accountservice.service;

import lombok.RequiredArgsConstructor;
import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.entity.Account;
import me.ghwn.netflix.accountservice.repository.AccountRepository;
import me.ghwn.netflix.accountservice.vo.AccountCreationRequest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public AccountDto createAccount(AccountCreationRequest request) {
        Account account = modelMapper.map(request, Account.class);
        accountRepository.save(account);
        return modelMapper.map(account, AccountDto.class);
    }

    @Override
    public Optional<AccountDto> getAccountDetail(Long id) {
        return accountRepository.findById(id)
                .map(account -> modelMapper.map(account, AccountDto.class));
    }
}