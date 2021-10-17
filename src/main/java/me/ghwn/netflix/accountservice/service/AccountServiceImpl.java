package me.ghwn.netflix.accountservice.service;

import lombok.RequiredArgsConstructor;
import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.entity.Account;
import me.ghwn.netflix.accountservice.exception.AccountNotFoundException;
import me.ghwn.netflix.accountservice.repository.AccountRepository;
import me.ghwn.netflix.accountservice.vo.AccountCreationRequest;
import me.ghwn.netflix.accountservice.vo.AccountUpdateRequest;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Page<AccountDto> getAccountList(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(account -> modelMapper.map(account, AccountDto.class));
    }

    @Transactional
    @Override
    public AccountDto updateAccount(Long id, AccountUpdateRequest request) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException());
        modelMapper.map(request, account);
        return modelMapper.map(account, AccountDto.class);
    }

    @Transactional
    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException());
        accountRepository.delete(account);
    }
}
