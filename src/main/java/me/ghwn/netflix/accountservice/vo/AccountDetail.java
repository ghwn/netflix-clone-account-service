package me.ghwn.netflix.accountservice.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
public class AccountDetail {

    private Long id;

    private String email;

    private boolean active;

    private Set<String> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}