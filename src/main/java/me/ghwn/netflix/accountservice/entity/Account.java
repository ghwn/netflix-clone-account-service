package me.ghwn.netflix.accountservice.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@EqualsAndHashCode(of = "id", callSuper = false)
@AllArgsConstructor
@Getter @Setter @NoArgsConstructor
@Entity
public class Account extends TimestampedEntity {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<AccountRole> roles = Set.of(AccountRole.USER);
}
