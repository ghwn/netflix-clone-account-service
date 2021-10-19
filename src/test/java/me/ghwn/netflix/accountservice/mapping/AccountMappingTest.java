package me.ghwn.netflix.accountservice.mapping;

import me.ghwn.netflix.accountservice.entity.Account;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import me.ghwn.netflix.accountservice.dto.AccountCreationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AccountMappingTest {

    @Autowired ModelMapper modelMapper;

    @Test
    @DisplayName("Convert AccountCreationRequest to Entity")
    void accountCreationRequestToEntity() {
        String email = "admin@example.com";
        String password = "P@ssw0rd1234";

        AccountCreationRequest request = new AccountCreationRequest();
        request.setEmail(email);
        request.setPassword(password);

        Account account = modelMapper.map(request, Account.class);
        assertThat(account.getEmail()).isEqualTo(email);
        assertThat(account.getPassword()).isEqualTo(password);
        assertThat(account.isActive()).isTrue();
        assertThat(account.getRoles()).containsExactly(AccountRole.USER);
    }

    @Test
    @DisplayName("Convert HashMap that contains only required fields to Entity")
    void minimumFieldsToEntity() {
        String email = "admin@example.com";
        String password = "P@ssw0rd1234";

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);

        Account account = modelMapper.map(request, Account.class);
        assertThat(account.getEmail()).isEqualTo(email);
        assertThat(account.getPassword()).isEqualTo(password);
        assertThat(account.isActive()).isTrue();
        assertThat(account.getRoles()).containsExactly(AccountRole.USER);
    }
}
