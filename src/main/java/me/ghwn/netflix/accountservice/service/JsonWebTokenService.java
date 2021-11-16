package me.ghwn.netflix.accountservice.service;

import io.jsonwebtoken.Claims;

public interface JsonWebTokenService {

    String createAccessToken(String email, String secret, Long expirationTime);

    String createRefreshToken(String secret, Long expirationTime);

    Claims parseAccessToken(String accessToken, String secret);

    Claims parseRefreshToken(String refreshToken, String secret);
}
