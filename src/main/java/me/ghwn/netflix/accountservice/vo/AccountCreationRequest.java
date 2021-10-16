package me.ghwn.netflix.accountservice.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequest {

    private String email;

    private String password;

    private Boolean active;

    private Set<String> roles;
}
