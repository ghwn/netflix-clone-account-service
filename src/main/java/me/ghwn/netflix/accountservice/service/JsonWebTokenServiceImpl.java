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
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class JsonWebTokenServiceImpl implements JsonWebTokenService {

    private static final String ACCOUNT_ID_PAYLOAD_KEY = "aid";

    private final RefreshTokenRepository refreshTokenRepository;
    private final ModelMapper modelMapper;

    @Override
    public String createAccessToken(String claim, String secret, Long expirationTime) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, "JWT")
                .claim(ACCOUNT_ID_PAYLOAD_KEY, claim)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (expirationTime * 1000)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    @Transactional
    @Override
    public String createRefreshToken(String accountId, String secret, Long expirationTime) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, "JWT")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (expirationTime * 1000)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        Optional<RefreshToken> refreshTokenQueried = refreshTokenRepository.findByAccountId(accountId);
        RefreshToken refreshTokenEntity = null;
        if (refreshTokenQueried.isPresent()) {
            refreshTokenEntity = refreshTokenQueried.get();
            refreshTokenEntity.setValue(refreshToken);
        } else {
            refreshTokenEntity = new RefreshToken(null, accountId, refreshToken);
        }
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshTokenEntity);
        return savedRefreshToken.getValue();
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
    public RefreshTokenDto getRefreshToken(String accountId) {
        return refreshTokenRepository.findByAccountId(accountId)
                .map(refreshToken -> modelMapper.map(refreshToken, RefreshTokenDto.class))
                .orElseThrow(RefreshTokenNotFoundException::new);
    }
}
