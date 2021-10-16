package me.ghwn.netflix.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import me.ghwn.netflix.accountservice.vo.AccountCreationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // TODO: Add Spring REST Docs
    @Test
    @DisplayName("Create new account successfully with required fields")
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
    @DisplayName("Create new account successfully with all required and optional fields")
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
}
