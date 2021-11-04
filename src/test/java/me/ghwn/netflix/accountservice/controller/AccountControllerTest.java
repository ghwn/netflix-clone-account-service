package me.ghwn.netflix.accountservice.controller;

import me.ghwn.netflix.accountservice.dto.AccountDto;
import me.ghwn.netflix.accountservice.dto.AccountUpdateRequest;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
import me.ghwn.netflix.accountservice.entity.Account;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import me.ghwn.netflix.accountservice.repository.AccountRepository;
import me.ghwn.netflix.accountservice.security.WithMockAccountContext;
import me.ghwn.netflix.accountservice.service.AccountService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountControllerTest extends BaseControllerTest {

    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;

    @Test
    @DisplayName("Create new account successfully by passing required fields only")
    void createAccountWithRequiredFields() throws Exception {
        String email = "admin@example.com";
        String password = "P@ssw0rd1234";

        SignupRequest request = new SignupRequest();
        request.setEmail(email);
        request.setPassword(password);

        mockMvc.perform(post("/api/v1/accounts")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Create new account with invalid account role")
    void createAccountWithInvalidAccountRole() throws Exception {
        String email = "admin@example.com";
        String password = "P@ssw0rd1234";
        boolean active = false;
        Set<String> roles = Set.of("USER", "HELLO");

        SignupRequest request = new SignupRequest(email, password, active, roles);
        mockMvc.perform(post("/api/v1/accounts")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create new account successfully by passing all required and optional fields")
    void createAccount() throws Exception {
        String email = "admin@example.com";
        String password = "P@ssw0rd1234";
        Set<String> roles = Set.of("USER", "ADMIN");
        boolean active = false;

        SignupRequest request = new SignupRequest();
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
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())

                .andDo(documentHandler.document(
                        requestFields(
                                fieldWithPath("email").description("Email of new account"),
                                fieldWithPath("password").description("Password of new account"),
                                fieldWithPath("active").description("Whether the account is active or not"),
                                fieldWithPath("roles").description("Authorities of new account")
                        ),
                        responseFields(
                                fieldWithPath("id").description("ID of new account"),
                                fieldWithPath("email").description("Email of new account"),
                                fieldWithPath("active").description("Whether the account is active or not"),
                                fieldWithPath("roles").description("Authorities of new account"),
                                fieldWithPath("createdAt").description("Created date and time of the account"),
                                fieldWithPath("updatedAt").description("Last updated date and time of the account"),
                                fieldWithPath("_links.self.href").description("Link to <<resources_accounts_create, self>>"),
                                fieldWithPath("_links.create-account.href").description("Link to <<resources_accounts_create, create new account>>"),
                                fieldWithPath("_links.profile.href").description("Link to document")
                        ),
                        links(
                                linkWithRel("self").description("Link to <<resources-accounts_create, self>>"),
                                linkWithRel("create-account").description("Link to <<resources_accounts_create, create new account>>"),
                                linkWithRel("profile").description("Link to document")
                        )
                ));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Try to create new account with email that is null or empty")
    void createAccountWithEmptyEmail(String email) throws Exception {
        String password = "P@ssw0rd1234";
        boolean active = false;
        Set<String> roles = Set.of("USER", "ADMIN");

        SignupRequest request = new SignupRequest(email, password, active, roles);

        mockMvc.perform(post("/api/v1/accounts")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[*].message").exists())
                .andExpect(jsonPath("_links.index.href").exists())
                .andDo(print());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Try to create new account with password that is null or empty")
    void createAccountWithEmptyPassword(String password) throws Exception {
        String email = "admin@example.com";
        boolean active = false;
        Set<String> roles = Set.of("USER", "ADMIN");

        SignupRequest request = new SignupRequest(email, password, active, roles);

        mockMvc.perform(post("/api/v1/accounts")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("Get an existing account successfully")
    @WithMockAccountContext
    void getAccountDetail() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "user@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name())
        ));

        mockMvc.perform(get("/api/v1/accounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").value(account.getId()))
                .andExpect(jsonPath("email").value(account.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("active").value(account.isActive()))
                .andExpect(jsonPath("roles").value(new ArrayList<>(account.getRoles())))
                .andExpect(jsonPath("createdAt").isNotEmpty())
                .andExpect(jsonPath("updatedAt").isNotEmpty())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").doesNotExist())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.update-account.href").exists())
                .andExpect(jsonPath("_links.delete-account.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())

                .andDo(documentHandler.document(
                        responseFields(
                                fieldWithPath("id").description("ID of account"),
                                fieldWithPath("email").description("Email of account"),
                                fieldWithPath("active").description("Whether the account is active or not"),
                                fieldWithPath("roles").description("Authorities of account"),
                                fieldWithPath("createdAt").description("Created date and time of account"),
                                fieldWithPath("updatedAt").description("Last updated date and time of account"),
                                fieldWithPath("_links.self.href").description("Link to <<resources_account_retrieve, self>>"),
                                fieldWithPath("_links.create-account.href").description("Link to <<resources_accounts_create, create new account>>"),
                                fieldWithPath("_links.update-account.href").description("Link to <<resources_account_update, update an existing account>>"),
                                fieldWithPath("_links.delete-account.href").description("Link to <<resources_account_delete, delete an existing account>>"),
                                fieldWithPath("_links.profile.href").description("Link to document")
                        ),
                        links(
                                linkWithRel("self").description("Link to <<resources_account_retrieve, self>>"),
                                linkWithRel("create-account").description("Link to <<resources_accounts_create, create new account>>"),
                                linkWithRel("update-account").description("Link to <<resources_account_update, update an existing account>>"),
                                linkWithRel("delete-account").description("Link to <<resources_account_delete, delete an existing account>>"),
                                linkWithRel("profile").description("Link to document")
                        )
                ));
    }

    @Test
    @DisplayName("Try to get other account detail with user role")
    @WithMockAccountContext(email = "user1@example.com")
    void getOtherAccountDetailWithUserRole() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "user2@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name())
        ));
        mockMvc.perform(get("/api/v1/accounts/{id}", account.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Try to update other account with user role")
    @WithMockAccountContext(email = "user1@example.com")
    void updateOtherAccountWithUserRole() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "user2@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name())
        ));
        AccountUpdateRequest request = new AccountUpdateRequest(
                "newP@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name(), AccountRole.ADMIN.name())
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", account.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Try to delete other account with user role")
    @WithMockAccountContext(email = "user1@example.com")
    void deleteOtherAccountWithUserRole() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "user2@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name())
        ));
        mockMvc.perform(delete("/api/v1/accounts/{id}", account.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get other account detail with admin role")
    @WithMockAccountContext(email = "user1@example.com", roles = {"USER", "ADMIN"})
    void getOtherAccountDetailWithAdminRole() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "user2@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name())
        ));
        mockMvc.perform(get("/api/v1/accounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").exists())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.update-account.href").exists())
                .andExpect(jsonPath("_links.delete-account.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists());
    }

    @Test
    @DisplayName("Update other account with admin role")
    @WithMockAccountContext(email = "user1@example.com", roles = {"USER", "ADMIN"})
    void updateOtherAccountWithAdminRole() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "user2@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name())
        ));
        AccountUpdateRequest request = new AccountUpdateRequest(
                "newP@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name(), AccountRole.ADMIN.name())
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", account.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").exists())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.delete-account.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists());
    }

    @Test
    @DisplayName("Delete other account with admin role")
    @WithMockAccountContext(email = "admin@example.com", roles = {"USER", "ADMIN"})
    void deleteOtherAccountWithAdminRole() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "user@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name())
        ));
        mockMvc.perform(delete("/api/v1/accounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists());
    }

    @Test
    @DisplayName("Try to get non-existent account")
    @WithMockAccountContext(email = "user@example.com", roles = {"USER"})
    void getNonExistentAccountDetail() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/10"))
                .andExpect(header().stringValues(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Get account list successfully")
    @WithMockAccountContext(email = "admin@example.com", roles = {"USER", "ADMIN"})
    void getAccountList() throws Exception {
        for (int i = 0; i < 100; i++) {
            String email = String.format("admin%d@example.com", (i + 1));
            accountService.createAccount(new SignupRequest(
                    email,
                    "P@ssw0rd1234",
                    true,
                    Set.of(AccountRole.USER.name())
            ));
        }

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("_embedded.accounts[*].id").exists())
                .andExpect(jsonPath("_embedded.accounts[*].email").exists())
                .andExpect(jsonPath("_embedded.accounts[*].password").doesNotExist())
                .andExpect(jsonPath("_embedded.accounts[*].active").exists())
                .andExpect(jsonPath("_embedded.accounts[*].roles").exists())
                .andExpect(jsonPath("_embedded.accounts[*].createdAt").exists())
                .andExpect(jsonPath("_embedded.accounts[*].updatedAt").exists())
                .andExpect(jsonPath("_embedded.accounts[*]._links.self.href").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.first.href").exists())
                .andExpect(jsonPath("_links.next.href").exists())
                .andExpect(jsonPath("_links.last.href").exists())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())

                .andDo(documentHandler.document(
                        responseFields(
                                fieldWithPath("_embedded.accounts[].id").description("ID of account"),
                                fieldWithPath("_embedded.accounts[].email").description("Email of account"),
                                fieldWithPath("_embedded.accounts[].active").description("Whether the account is active or not"),
                                fieldWithPath("_embedded.accounts[].roles").description("Authorities of account"),
                                fieldWithPath("_embedded.accounts[].createdAt").description("Created date and time of the account"),
                                fieldWithPath("_embedded.accounts[].updatedAt").description("Last updated date and time of the account"),
                                fieldWithPath("_embedded.accounts[]._links.self.href").description("Link to the account"),
                                fieldWithPath("_links.self.href").description("Link to <<resources_accounts_create, self>>"),
                                fieldWithPath("_links.first.href").description("Link to the first page"),
                                fieldWithPath("_links.next.href").description("Link to the next page"),
                                fieldWithPath("_links.last.href").description("Link to the last page"),
                                fieldWithPath("_links.create-account.href").description("Link to <<resources_accounts_create, create new account>>"),
                                fieldWithPath("_links.profile.href").description("Link to document"),
                                fieldWithPath("page.size").description("The number of elements per one page"),
                                fieldWithPath("page.totalElements").description("The number of total elements"),
                                fieldWithPath("page.totalPages").description("The number of total pages"),
                                fieldWithPath("page.number").description("Current page number")
                        ),
                        links(
                                linkWithRel("self").description("Link to <<resources_accounts_create, self>>"),
                                linkWithRel("first").description("Link to the first page"),
                                linkWithRel("next").description("Link to the next page"),
                                linkWithRel("last").description("Link to the last page"),
                                linkWithRel("create-account").description("Link to <<resources_accounts_create, create new account>>"),
                                linkWithRel("profile").description("Link to document")
                        )
                ));
    }

    @Test
    @DisplayName("Update an existing account successfully")
    @WithMockAccountContext(email = "user@example.com", roles = {"USER"})
    void updateAccount() throws Exception {
        // given
        String oldEmail = "user@example.com";
        String oldPassword = "P@ssw0rd1234";
        boolean oldActive = false;
        Set<AccountRole> oldRoles = Set.of(AccountRole.USER);
        Account account = new Account(null, oldEmail, oldPassword, oldActive, oldRoles);
        accountRepository.save(account);

        String newPassword = "newP@ssw0rd1234";
        boolean newActive = !oldActive;
        Set<String> newRoles = Set.of(AccountRole.USER.name(), AccountRole.ADMIN.name());
        AccountUpdateRequest request = new AccountUpdateRequest(newPassword, newActive, newRoles);

        // when & then
        mockMvc.perform(put("/api/v1/accounts/{id}", account.getId())
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(account.getId()))
                .andExpect(jsonPath("email").value(account.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("active").value(newActive))
                .andExpect(jsonPath("roles", Matchers.hasItem(AccountRole.USER.name())))
                .andExpect(jsonPath("roles", Matchers.hasItem(AccountRole.ADMIN.name())))
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").doesNotExist())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.get-account-detail.href").exists())
                .andExpect(jsonPath("_links.delete-account.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())

                .andDo(documentHandler.document(
                        requestFields(
                                fieldWithPath("password").description("New password of account"),
                                fieldWithPath("active").description("New active status of account"),
                                fieldWithPath("roles").description("New authorities of account")
                        ),
                        responseFields(
                                fieldWithPath("id").description("ID of new account"),
                                fieldWithPath("email").description("Email of new account"),
                                fieldWithPath("active").description("Whether the account is active or not"),
                                fieldWithPath("roles").description("Authorities of new account"),
                                fieldWithPath("createdAt").description("Created date and time of the account"),
                                fieldWithPath("updatedAt").description("Last updated date and time of the account"),
                                fieldWithPath("_links.self.href").description("Link to <<resources_account_retrieve, self>>"),
                                fieldWithPath("_links.create-account.href").description("Link to <<resources_accounts_create, create new account>>"),
                                fieldWithPath("_links.get-account-detail.href").description("Link to <<resources_account_retrieve, get account detail>>"),
                                fieldWithPath("_links.delete-account.href").description("Link to <<resources_account_delete, delete an existing account>>"),
                                fieldWithPath("_links.profile.href").description("Link to document")
                        ),
                        links(
                                linkWithRel("self").description("Link to <<resources_account_retrieve, self>>"),
                                linkWithRel("create-account").description("Link to <<resources_accounts_create, create new account>>"),
                                linkWithRel("get-account-detail").description("Link to <<resources_account_retrieve, get account detail>>"),
                                linkWithRel("delete-account").description("Link to <<resources_account_delete, delete an existing account>>"),
                                linkWithRel("profile").description("Link to document")
                        )
                ));
    }

    @Test
    @DisplayName("Try to update non-existent account")
    @WithMockAccountContext(email = "user@example.com", roles = {"USER"})
    void updateNonExistentAccount() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest("newP@ssw0rd1234", true, Set.of(AccountRole.USER.name()));

        // when & then
        mockMvc.perform(put("/api/v1/accounts/123123")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Delete an existing account successfully (by user)")
    @WithMockAccountContext(email = "user@example.com", roles = {"USER"})
    void deleteAccount() throws Exception {
        Account account = new Account(null, "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER));
        accountRepository.save(account);

        mockMvc.perform(delete("/api/v1/accounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").doesNotExist())

                .andDo(documentHandler.document(
                        responseFields(
                                fieldWithPath("_links.create-account.href").description("Link to <<resources_accounts_create, create new account>>"),
                                fieldWithPath("_links.profile.href").description("Link to document")
                        ),
                        links(
                                linkWithRel("create-account").description("Link to <<resources_accounts_create, create new account>>"),
                                linkWithRel("profile").description("Link to document")
                        )
                ));
    }

    @Test
    @DisplayName("Delete an existing account successfully (by admin)")
    @WithMockAccountContext(email = "admin@example.com", roles = {"USER", "ADMIN"})
    void deleteAccountByAdmin() throws Exception {
        Account account = new Account(null, "user@example.com", "P@ssw0rd1234", true, Set.of(AccountRole.USER));
        accountRepository.save(account);

        mockMvc.perform(delete("/api/v1/accounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.create-account.href").exists())
                .andExpect(jsonPath("_links.get-account-list.href").exists())

                .andDo(documentHandler.document(
                        responseFields(
                                fieldWithPath("_links.create-account.href").description("Link to <<resources_accounts_create, create new account>>"),
                                fieldWithPath("_links.get-account-list.href").description("Link to <<resources_accounts_list, get account list>>"),
                                fieldWithPath("_links.profile.href").description("Link to document")
                        ),
                        links(
                                linkWithRel("create-account").description("Link to <<resources_accounts_create, create new account>>"),
                                linkWithRel("get-account-list").description("Link to <<resources_accounts_list, get account list>>"),
                                linkWithRel("profile").description("Link to document")
                        )
                ));
    }

    @Test
    @DisplayName("Try to delete non-existent account")
    @WithMockAccountContext(email = "user@example.com", roles = {"USER"})
    void deleteNonExistentAccount() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/10"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("_embedded.accounts array should not be disappeared when there are no saved accounts in database")
    @WithMockAccountContext(email = "admin@example.com", roles = {"USER", "ADMIN"})
    void keepEmbeddedAccountsArrayWhenNoAccounts() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(jsonPath("_embedded.accounts").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("Update an existing account to have only admin role")
    @WithMockAccountContext(email = "admin@example.com", roles = {"USER", "ADMIN"})
    void updateAccountToHaveOnlyAdminRole() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "admin@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name(), AccountRole.ADMIN.name())
        ));

        AccountUpdateRequest request = modelMapper.map(account, AccountUpdateRequest.class);
        request.setRoles(Set.of(AccountRole.ADMIN.name()));
        mockMvc.perform(put("/api/v1/accounts/{id}", account.getId())
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("roles", hasItem(AccountRole.ADMIN.name())))
                .andExpect(jsonPath("roles", not(hasItem(AccountRole.USER.name()))))
                .andExpect(jsonPath("roles", hasSize(1)));
    }

    @Test
    @DisplayName("Update an existing account to have only user role")
    @WithMockAccountContext(email = "admin@example.com", roles = {"USER", "ADMIN"})
    void updateAccountToHaveOnlyUserRole() throws Exception {
        AccountDto account = accountService.createAccount(new SignupRequest(
                "admin@example.com",
                "P@ssw0rd1234",
                true,
                Set.of(AccountRole.USER.name(), AccountRole.ADMIN.name())
        ));

        AccountUpdateRequest request = modelMapper.map(account, AccountUpdateRequest.class);
        request.setRoles(Set.of(AccountRole.USER.name()));
        mockMvc.perform(put("/api/v1/accounts/{id}", account.getId())
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("roles", hasItem(AccountRole.USER.name())))
                .andExpect(jsonPath("roles", not(hasItem(AccountRole.ADMIN.name()))))
                .andExpect(jsonPath("roles", hasSize(1)));
    }

    @Test
    @DisplayName("Try to update an existing account with invalid account role")
    @WithMockAccountContext(email = "user@example.com", roles = {"USER"})
    void updateAccountWithInvalidRoles() throws Exception {
        Set<AccountRole> accountRolesBeforeUpdate = Set.of(AccountRole.USER);
        Account account = new Account(null, "user@example.com", "P@ssw0rd1234", true, accountRolesBeforeUpdate);
        accountRepository.save(account);

        AccountUpdateRequest request = modelMapper.map(account, AccountUpdateRequest.class);
        request.setRoles(Set.of("HELLO"));

        mockMvc.perform(put("/api/v1/accounts/{id}", account.getId())
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        Account updatedAccount = accountRepository.findById(account.getId()).get();
        assertThat(updatedAccount.getRoles()).isEqualTo(accountRolesBeforeUpdate);
    }
}
