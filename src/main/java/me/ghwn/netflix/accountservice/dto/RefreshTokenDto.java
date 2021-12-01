package me.ghwn.netflix.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter @Setter @NoArgsConstructor
public class RefreshTokenDto {

    private Long id;

    private String email;

    private String value;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
