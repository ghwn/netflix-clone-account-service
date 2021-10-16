package me.ghwn.netflix.accountservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    @Test
    @DisplayName("Two account instances with the same ID should be equal to each other")
    void equals() {
        Account account1 = new Account();
        account1.setId(1L);

        Account account2 = new Account();
        account2.setId(1L);

        assertThat(account1).isEqualTo(account2);
    }
}
