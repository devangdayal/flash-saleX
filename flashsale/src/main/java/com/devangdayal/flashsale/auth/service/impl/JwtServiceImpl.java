package com.devangdayal.flashsale.auth.service.impl;

import org.springframework.stereotype.Service;
import com.devangdayal.flashsale.auth.service.JwtService;
import com.devangdayal.flashsale.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;


    @Override
    public String generateToken(User user){
        return Jwts.builder()
            .subject(user.getEmail())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis()+expiration))
            .signWith(getSigningKey())
            .compact();
    }

    @Override
    public String extractUsername(String token){
        return extractClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, User user){
       String username = extractUsername(token);
       return username.equals(user.getEmail()) && !extractClaims(token).getExpiration().before(new Date());
    }

    @Override
    public long getExpirationTime(){
        return expiration/1000;
    }

    private Claims extractClaims(String token){
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
    }

}
