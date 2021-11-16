package me.ghwn.netflix.accountservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;

@Transactional(readOnly = true)
@Service
public class JsonWebTokenServiceImpl implements JsonWebTokenService {

    @Override
    public String createAccessToken(String email, String secret, Long expirationTime) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder()
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (expirationTime * 1000)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public String createRefreshToken(String secret, Long expirationTime) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (expirationTime * 1000)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public Claims parseAccessToken(String accessToken, String secret) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }

    @Override
    public Claims parseRefreshToken(String refreshToken, String secret) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJwt(refreshToken)
                .getBody();
    }
}
