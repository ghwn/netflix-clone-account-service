package me.ghwn.netflix.accountservice.controller;

import lombok.RequiredArgsConstructor;
import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.exception.AccountNotFoundException;
import me.ghwn.netflix.accountservice.service.AccountService;
import me.ghwn.netflix.accountservice.vo.AccountCreationRequest;
import me.ghwn.netflix.accountservice.vo.AccountDetail;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final ModelMapper modelMapper;

    // FIXME: Handle errors appropriately
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreationRequest request, Errors errors) {
        AccountDto createdAccountDto = accountService.createAccount(request);

        EntityModel<AccountDetail> content = EntityModel.of(modelMapper.map(createdAccountDto, AccountDetail.class));
        Link selfLink = linkTo(getClass()).slash(createdAccountDto.getId()).withSelfRel();
        content.add(selfLink);
        content.add(Link.of("/docs/index.html#resources-accounts-create").withRel("profile"));
        content.add(linkTo(getClass()).withRel("get-account-list"));
        return ResponseEntity.created(selfLink.toUri()).body(content);
    }

    // FIXME: Return 404 error response when requested account does not exist.
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
}