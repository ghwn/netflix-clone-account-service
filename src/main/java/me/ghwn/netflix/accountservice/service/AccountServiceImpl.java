package me.ghwn.netflix.accountservice.service;

import lombok.RequiredArgsConstructor;
import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.dto.AccountUpdateRequest;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
import me.ghwn.netflix.accountservice.entity.Account;
import me.ghwn.netflix.accountservice.exception.AccountNotFoundException;
import me.ghwn.netflix.accountservice.repository.AccountRepository;
import me.ghwn.netflix.accountservice.security.AccountContext;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        Set<SimpleGrantedAuthority> authorities = account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
        return new AccountContext(account, authorities);
    }

    @Transactional
    @Override
    public AccountDto createAccount(SignupRequest request) {
        Account account = modelMapper.map(request, Account.class);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setAccountId(UUID.randomUUID().toString());
        accountRepository.save(account);
        return modelMapper.map(account, AccountDto.class);
    }

    @Override
    public AccountDto getAccountById(Long id) {
        return accountRepository.findById(id)
                .map(account -> modelMapper.map(account, AccountDto.class))
                .orElseThrow(() -> new AccountNotFoundException());
    }

    @Override
    public AccountDto getAccountByAccountId(String accountId) {
        return accountRepository.findByAccountId(accountId)
                .map(account -> modelMapper.map(account, AccountDto.class))
                .orElseThrow(() -> new AccountNotFoundException());
    }

    @Override
    public AccountDto getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .map(account -> modelMapper.map(account, AccountDto.class))
                .orElseThrow(() -> new AccountNotFoundException());
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
