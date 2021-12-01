package me.ghwn.netflix.accountservice.mapping;

import me.ghwn.netflix.accountservice.entity.Account;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import me.ghwn.netflix.accountservice.dto.SignupRequest;
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

    @DisplayName("Convert AccountCreationRequest to Entity")
    @Test
    void accountCreationRequestToEntity() {
        String email = "admin@example.com";
        String password = "P@ssw0rd1234";

        SignupRequest request = new SignupRequest();
        request.setEmail(email);
        request.setPassword(password);

        Account account = modelMapper.map(request, Account.class);
        assertThat(account.getEmail()).isEqualTo(email);
        assertThat(account.getPassword()).isEqualTo(password);
        assertThat(account.isActive()).isTrue();
        assertThat(account.getRoles()).containsExactly(AccountRole.USER);
    }

    @DisplayName("Convert HashMap that contains only required fields to Entity")
    @Test
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
