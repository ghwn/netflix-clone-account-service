package me.ghwn.netflix.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter @NoArgsConstructor
public class LoginRequest {

    private String email;

    private String password;
}
