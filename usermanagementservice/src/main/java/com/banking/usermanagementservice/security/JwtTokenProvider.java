package com.banking.usermanagementservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.security.jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigninKey(){
        byte [] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Generate access token */
    public String generateToken(UserDetails userDetails, UUID userId){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Generate refresh token*/
    public String generateRefreshToken(UserDetails userDetails, UUID userID){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userID.toString());
        claims.put("type", "refresh");

        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }


    /**
     * Create JWT token*/
    public String createToken(Map<String , Object> claims, String subject, long expiration){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);


        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from token*/
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract userId from token*/
    public UUID extractUserId(String token){
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userIdStr);
    }

    /**
     * Extract expiration date from token*/
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claims from token*/
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extrctAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token*/

    private Claims extrctAllClaims(String token){
        return Jwts.parser() // Changed from parserBuilder()
                .verifyWith(getSigninKey()) // Changed from setSigningKey()
                .build()
                .parseSignedClaims(token) // Changed from parseClaimsJws()
                .getPayload();
    }

    /**
     * Check if token expired*/
    public Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token*/
    public Boolean validateToken(String token, UserDetails userDetails){
        try{
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e){
            log.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * Check if token is a refresh token*/
    public Boolean isRefreshToken(String token){
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }
}
