package me.ghwn.netflix.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.ghwn.netflix.accountservice.entity.AccountRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Set;

@AllArgsConstructor
@Getter @Setter @NoArgsConstructor
public class SignupRequest {

    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email address is required")
    private String email;

    @NotEmpty(message = "Password is required")
    @Size(min = 8, message = "Password requires at least 8 characters")
    private String password;

    private Boolean active;

    private Set<AccountRole> roles;

}
