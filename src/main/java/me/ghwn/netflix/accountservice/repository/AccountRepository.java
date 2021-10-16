package me.ghwn.netflix.accountservice.repository;

import me.ghwn.netflix.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
