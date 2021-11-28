package me.ghwn.netflix.accountservice.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import me.ghwn.netflix.accountservice.dto.RefreshTokenDto;
import me.ghwn.netflix.accountservice.entity.RefreshToken;
import me.ghwn.netflix.accountservice.exception.RefreshTokenNotFoundException;
import me.ghwn.netflix.accountservice.repository.RefreshTokenRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class JsonWebTokenServiceImpl implements JsonWebTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final ModelMapper modelMapper;

    @Override
    public String createAccessToken(String email, String secret, Long expirationTime) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, "JWT")
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (expirationTime * 1000)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    @Transactional
    @Override
    public String createRefreshToken(String email, String secret, Long expirationTime) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, "JWT")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (expirationTime * 1000)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
        RefreshToken refreshTokenEntity = new RefreshToken(null, email, refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);
        return refreshToken;
    }

    @Override
    public Jws<Claims> parseAccessToken(String accessToken, String secret) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken);
    }

    @Override
    public Jws<Claims> parseRefreshToken(String refreshToken, String secret) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(refreshToken);
    }

    @Override
    public RefreshTokenDto getRefreshToken(String email) {
        return refreshTokenRepository.findByEmail(email)
                .map(refreshToken -> modelMapper.map(refreshToken, RefreshTokenDto.class))
                .orElseThrow(() -> new RefreshTokenNotFoundException());
    }
}
