package me.ghwn.netflix.accountservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import me.ghwn.netflix.accountservice.dto.RefreshTokenDto;

public interface JsonWebTokenService {

    String createAccessToken(String email, String secret, Long expirationTime);

    String createRefreshToken(String email, String secret, Long expirationTime);

    Jws<Claims> parseAccessToken(String accessToken, String secret);

    Jws<Claims> parseRefreshToken(String refreshToken, String secret);

    RefreshTokenDto getRefreshToken(String email);
}
