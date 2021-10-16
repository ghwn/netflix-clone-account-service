package me.ghwn.netflix.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ghwn.netflix.accountservice.entity.Account;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import me.ghwn.netflix.accountservice.repository.AccountRepository;
import me.ghwn.netflix.accountservice.vo.AccountCreationRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;

    // TODO: Add docs
    @Test
    @DisplayName("Create new account successfully by passing required fields only")
    void createAccountWithRequiredFields() throws Exception {
        String email = "admin@example.com";
        String password = "P@ssw0rd1234";

        AccountCreationRequest request = new AccountCreationRequest();
        request.setEmail(email);
        request.setPassword(password);

        mockMvc.perform(post("/api/v1/accounts")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("active").value(true))
                .andExpect(jsonPath("roles", hasItem(AccountRole.USER.name())))
                .andExpect(jsonPath("roles", not(hasItem(AccountRole.ADMIN.name()))))
                .andExpect(jsonPath("createdAt", notNullValue()))
                .andExpect(jsonPath("updatedAt", notNullValue()))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("Create new account successfully by passing all required and optional fields")
    void createAccountWithAllFields() throws Exception {
        String email = "admin@example.com";
        String password = "P@ssw0rd1234";
        Set<String> roles = Set.of("USER", "ADMIN");
        boolean active = false;

        AccountCreationRequest request = new AccountCreationRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setActive(active);
        request.setRoles(roles);

        mockMvc.perform(post("/api/v1/accounts")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("active").value(active))
                .andExpect(jsonPath("roles", hasItem(AccountRole.USER.name())))
                .andExpect(jsonPath("roles", hasItem(AccountRole.ADMIN.name())))
                .andExpect(jsonPath("createdAt", notNullValue()))
                .andExpect(jsonPath("updatedAt", notNullValue()))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").exists())
                .andDo(print());
    }

    // TODO: Add docs
    @Test
    @DisplayName("Get an existing account successfully")
    void getAccountDetail() throws Exception {
        Account account = new Account(null, "admin@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER));
        accountRepository.save(account);

        mockMvc.perform(get("/api/v1/accounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").value(account.getId()))
                .andExpect(jsonPath("email").value(account.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("active").value(account.isActive()))
                .andExpect(jsonPath("roles").value(account.getRoles().stream().map(role -> role.name()).collect(Collectors.toList())))
                .andExpect(jsonPath("createdAt").isNotEmpty())
                .andExpect(jsonPath("updatedAt").isNotEmpty())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").exists())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.update-account.href").exists())
                .andExpect(jsonPath("_links.delete-account.href").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("Try to get non-existing account")
    @Disabled
    void getNonExistingAccountDetail() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/10"))
                .andExpect(header().stringValues(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errors").exists())
                .andDo(print());
    }
}
