package me.ghwn.netflix.accountservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ghwn.netflix.accountservice.dto.AccountDetail;
import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.dto.AccountUpdateRequest;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
import me.ghwn.netflix.accountservice.service.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@RestController
public class AccountController {

    private final AccountService accountService;
    private final ModelMapper modelMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> createAccount(@Valid @RequestBody SignupRequest request,
                                           BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        AccountDto createdAccountDto = accountService.createAccount(request);

        EntityModel<AccountDetail> content = EntityModel.of(modelMapper.map(createdAccountDto, AccountDetail.class));
        Link selfLink = linkTo(getClass()).slash(createdAccountDto.getAccountId()).withSelfRel();
        content.add(selfLink);
        content.add(linkTo(getClass()).withRel("create-account"));
        content.add(Link.of("/docs/index.html#resources-accounts-create").withRel("profile"));
        return ResponseEntity.created(selfLink.toUri()).body(content);
    }

    @GetMapping(value = "/{accountId}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> getAccountDetail(@PathVariable String accountId) {
        AccountDto accountDto = accountService.getAccountByAccountId(accountId);
        AccountDetail accountDetail = modelMapper.map(accountDto, AccountDetail.class);

        EntityModel<AccountDetail> content = EntityModel.of(accountDetail);
        Link selfLink = linkTo(getClass()).slash(accountId).withSelfRel();
        content.add(selfLink);
        content.add(Link.of("/docs/index.html#resources-account-detail").withRel("profile"));
        content.add(linkTo(getClass()).withRel("get-account-list"));
        content.add(linkTo(getClass()).withRel("create-account"));
        content.add(selfLink.withRel("update-account"));
        content.add(selfLink.withRel("delete-account"));
        return ResponseEntity.ok(content);
    }

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> getAccountList(Pageable pageable,
                                            PagedResourcesAssembler<AccountDto> assembler) {
        Page<AccountDto> accountList = accountService.getAccountList(pageable);

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

    @PutMapping(value = "/{accountId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> updateAccount(@PathVariable String accountId,
                                           @Valid @RequestBody AccountUpdateRequest request,
                                           BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        AccountDto accountDto = accountService.getAccountByAccountId(accountId);
        AccountDto updatedAccountDto = accountService.updateAccount(accountDto.getId(), request);

        EntityModel<AccountDetail> content = EntityModel.of(modelMapper.map(updatedAccountDto, AccountDetail.class));
        Link selfLink = linkTo(getClass()).slash(updatedAccountDto.getId()).withSelfRel();
        content.add(selfLink);
        content.add(Link.of("/docs/index.html#resources-account-update").withRel("profile"));
        content.add(linkTo(getClass()).withRel("get-account-list"));
        content.add(linkTo(getClass()).withRel("create-account"));
        content.add(linkTo(getClass()).slash(accountId).withRel("get-account-detail"));
        content.add(linkTo(getClass()).slash(accountId).withRel("delete-account"));
        return ResponseEntity.ok().body(content);
    }

    @DeleteMapping(value = "/{accountId}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> deleteAccount(@PathVariable String accountId) {
        AccountDto accountDto = accountService.getAccountByAccountId(accountId);
        accountService.deleteAccount(accountDto.getId());

        RepresentationModel<?> content = RepresentationModel.of(null);
        content.add(Link.of("/docs/index.html#resources-account-delete").withRel("profile"));
        content.add(linkTo(getClass()).withRel("create-account"));
        content.add(linkTo(getClass()).withRel("get-account-list"));
        return ResponseEntity.ok().body(content);
    }

}
