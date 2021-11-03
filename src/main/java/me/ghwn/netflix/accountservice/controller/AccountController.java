package me.ghwn.netflix.accountservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ghwn.netflix.accountservice.dto.AccountDetail;
import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.dto.AccountUpdateRequest;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import me.ghwn.netflix.accountservice.service.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final ModelMapper modelMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> createAccount(@Valid @RequestBody SignupRequest request,
                                           BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        validateAccountRoles(request.getRoles());

        AccountDto createdAccountDto = accountService.createAccount(request);

        EntityModel<AccountDetail> content = EntityModel.of(modelMapper.map(createdAccountDto, AccountDetail.class));
        Link selfLink = linkTo(getClass()).slash(createdAccountDto.getId()).withSelfRel();
        content.add(selfLink);
        content.add(selfLink.withRel("create-account"));
        content.add(Link.of("/docs/index.html#resources-accounts-create").withRel("profile"));
        return ResponseEntity.created(selfLink.toUri()).body(content);
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> getAccountDetail(@PathVariable Long id, @AuthenticationPrincipal User user) {
        AccountDto accountDto = accountService.getAccount(id);

        // Non-admin accounts cannot request other account's details.
        if (!hasAuthority(user, AccountRole.ADMIN) && !accountDto.getEmail().equals(user.getUsername())) {
            throw new AccessDeniedException("Access is denied");
        }

        AccountDetail accountDetail = modelMapper.map(accountDto, AccountDetail.class);
        EntityModel<AccountDetail> content = EntityModel.of(accountDetail);
        Link selfLink = linkTo(getClass()).slash(id).withSelfRel();
        content.add(selfLink);
        content.add(Link.of("/docs/index.html#resources-account-detail").withRel("profile"));
        if (hasAuthority(user, AccountRole.ADMIN)) {
            content.add(linkTo(getClass()).withRel("get-account-list"));
        }
        content.add(linkTo(getClass()).withRel("create-account"));
        content.add(selfLink.withRel("update-account"));
        content.add(selfLink.withRel("delete-account"));
        return ResponseEntity.ok(content);
    }

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> getAccountList(Pageable pageable,
                                            PagedResourcesAssembler<AccountDto> assembler,
                                            @AuthenticationPrincipal User user) {
        Page<AccountDto> accountList = accountService.getAccountList(pageable);

        // Non-admin cannot list accounts.
        if (!hasAuthority(user, AccountRole.ADMIN)) {
            throw new AccessDeniedException("Access is denied");
        }

        PagedModel<?> content = null;
        if (!accountList.hasContent()) {
            content = assembler.toEmptyModel(accountList, AccountDetail.class);
        } else {
            content = assembler.toModel(accountList, account -> {
                EntityModel<AccountDetail> model = EntityModel.of(modelMapper.map(account, AccountDetail.class));
                model.add(linkTo(getClass()).slash(account.getId()).withSelfRel());
                return model;
            });
        }

        Link selfLink = linkTo(getClass()).withSelfRel();
        content.add(selfLink.withRel("create-account"));
        content.add(Link.of("/docs/index.html#resources-accounts-list").withRel("profile"));
        return ResponseEntity.ok(content);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> updateAccount(@PathVariable Long id,
                                           @Valid @RequestBody AccountUpdateRequest request,
                                           BindingResult bindingResult,
                                           @AuthenticationPrincipal User user) throws BindException {
        AccountDto accountDto = accountService.getAccount(id);

        // Non-admin accounts cannot update other accounts.
        if (!hasAuthority(user, AccountRole.ADMIN) && !user.getUsername().equals(accountDto.getEmail())) {
            throw new AccessDeniedException("Access is denied");
        }

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        validateAccountRoles(request.getRoles());

        AccountDto updatedAccountDto = accountService.updateAccount(id, request);

        EntityModel<AccountDetail> content = EntityModel.of(modelMapper.map(updatedAccountDto, AccountDetail.class));
        Link selfLink = linkTo(getClass()).slash(updatedAccountDto.getId()).withSelfRel();
        content.add(selfLink);
        content.add(Link.of("/docs/index.html#resources-account-update").withRel("profile"));
        if (hasAuthority(user, AccountRole.ADMIN)) {
            content.add(linkTo(getClass()).withRel("get-account-list"));
        }
        content.add(linkTo(getClass()).withRel("create-account"));
        content.add(linkTo(getClass()).slash(id).withRel("get-account-detail"));
        content.add(linkTo(getClass()).slash(id).withRel("delete-account"));
        return ResponseEntity.ok().body(content);
    }

    @DeleteMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> deleteAccount(@PathVariable Long id,
                                           @AuthenticationPrincipal User user) {
        AccountDto accountDto = accountService.getAccount(id);

        // Non-admin accounts cannot delete other accounts.
        if (!hasAuthority(user, AccountRole.ADMIN) && !user.getUsername().equals(accountDto.getEmail())) {
            throw new AccessDeniedException("Access is denied");
        }

        accountService.deleteAccount(id);
        RepresentationModel<?> content = RepresentationModel.of(null);
        content.add(Link.of("/docs/index.html#resources-account-delete").withRel("profile"));
        content.add(linkTo(getClass()).withRel("create-account"));
        if (hasAuthority(user, AccountRole.ADMIN)) {
            content.add(linkTo(getClass()).withRel("get-account-list"));
        }
        return ResponseEntity.ok().body(content);
    }

    private void validateAccountRoles(Set<String> accountRoles) {
        if (accountRoles != null) {
            try {
                for (String accountRole : accountRoles) {
                    AccountRole.valueOf(accountRole.toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid account role contained");
            }
        }
    }

    private boolean hasAuthority(User user, AccountRole... requiredRoles) {
        if (user != null) {
            Set<AccountRole> accountRoleSet = user.getAuthorities().stream()
                    .map(authority -> AccountRole.valueOf(authority.getAuthority().replace("ROLE_", "")))
                    .collect(Collectors.toSet());
            Set<AccountRole> requiredRoleSet = Arrays.stream(requiredRoles).collect(Collectors.toSet());
            return accountRoleSet.stream().anyMatch(requiredRoleSet::contains);
        }
        return false;
    }
}
