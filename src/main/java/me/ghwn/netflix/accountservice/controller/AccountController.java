package me.ghwn.netflix.accountservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.exception.AccountNotFoundException;
import me.ghwn.netflix.accountservice.service.AccountService;
import me.ghwn.netflix.accountservice.vo.AccountCreationRequest;
import me.ghwn.netflix.accountservice.vo.AccountDetail;
import me.ghwn.netflix.accountservice.vo.AccountUpdateRequest;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final ModelMapper modelMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreationRequest request,
                                           BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        AccountDto createdAccountDto = accountService.createAccount(request);

        EntityModel<AccountDetail> content = EntityModel.of(modelMapper.map(createdAccountDto, AccountDetail.class));
        Link selfLink = linkTo(getClass()).slash(createdAccountDto.getId()).withSelfRel();
        content.add(selfLink);
        content.add(Link.of("/docs/index.html#resources-accounts-create").withRel("profile"));
        content.add(linkTo(getClass()).withRel("get-account-list"));
        return ResponseEntity.created(selfLink.toUri()).body(content);
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> getAccountDetail(@PathVariable Long id) {
        AccountDto accountDto = accountService.getAccountDetail(id)
                .orElseThrow(() -> new AccountNotFoundException());
        AccountDetail accountDetail = modelMapper.map(accountDto, AccountDetail.class);

        EntityModel<AccountDetail> content = EntityModel.of(accountDetail);
        Link selfLink = linkTo(getClass()).slash(id).withSelfRel();
        content.add(selfLink);
        content.add(Link.of("/docs/index.html#resources-account-detail").withRel("profile"));
        content.add(linkTo(getClass()).withRel("get-account-list"));
        content.add(linkTo(getClass()).withRel("create-account"));
        content.add(selfLink.withRel("update-account"));
        content.add(selfLink.withRel("delete-account"));
        return ResponseEntity.ok(content);
    }

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> getAccountList(Pageable pageable, PagedResourcesAssembler<AccountDto> assembler) {
        Page<AccountDto> accountList = accountService.getAccountList(pageable);

        PagedModel<EntityModel<AccountDetail>> body = assembler.toModel(accountList, account -> {
            EntityModel<AccountDetail> model = EntityModel.of(modelMapper.map(account, AccountDetail.class));
            model.add(linkTo(getClass()).slash(account.getId()).withSelfRel());
            return model;
        });

        Link selfLink = linkTo(getClass()).withSelfRel();
        body.add(selfLink.withRel("create-account"));
        body.add(Link.of("/docs/index.html#resources-accounts-list").withRel("profile"));
        return ResponseEntity.ok(body);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> updateAccount(@PathVariable Long id,
                                           @Valid @RequestBody AccountUpdateRequest request,
                                           BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        AccountDto updatedAccountDto = accountService.updateAccount(id, request);

        EntityModel<AccountDetail> content = EntityModel.of(modelMapper.map(updatedAccountDto, AccountDetail.class));
        Link selfLink = linkTo(getClass()).slash(updatedAccountDto.getId()).withSelfRel();
        content.add(selfLink);
        content.add(Link.of("/docs/index.html#resources-account-update").withRel("profile"));
        content.add(linkTo(getClass()).withRel("get-account-list"));
        content.add(linkTo(getClass()).withRel("create-account"));
        content.add(linkTo(getClass()).slash(id).withRel("get-account-detail"));
        content.add(linkTo(getClass()).slash(id).withRel("delete-account"));
        return ResponseEntity.ok().body(content);
    }
}
