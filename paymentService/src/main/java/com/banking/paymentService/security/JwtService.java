package com.banking.paymentService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    public UUID extractUserId(String token){
        String userIdString = extractClaim(token, claims -> claims.get("userId", String.class));
        return  UUID.fromString(userIdString);
    }

    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }


    public String extractRole(String token){
        return extractClaim(token, claims -> claims.get("role", String.class));
    }


    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token){
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e){
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private SecretKey getSigningKey(){
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
