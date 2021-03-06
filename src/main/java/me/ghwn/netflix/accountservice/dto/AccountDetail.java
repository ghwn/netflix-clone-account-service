package me.ghwn.netflix.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.ghwn.netflix.accountservice.entity.AccountRole;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;
import java.util.Set;

@Relation(collectionRelation = "accounts")
@AllArgsConstructor
@Getter @Setter @NoArgsConstructor
public class AccountDetail {

    private Long id;

    private String accountId;

    private String email;

    private boolean active;

    private Set<AccountRole> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
