package com.waregang.receiving_service.security.application;

import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.security.configuration.JwtProperties;
import com.waregang.receiving_service.security.exception.TokenExpiredException;
import com.waregang.receiving_service.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private SecretKey signInKey;

    @PostConstruct
    private void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        this.signInKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof User u) {
            claims.put("id", u.getId());
            claims.put("warehouseId", u.getWarehouseId());
            claims.put("nickname", u.getNickname());
            claims.put("authorities", u.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());
        }
        return buildToken(claims, userDetails, jwtProperties.accessTokenExpiration());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.refreshTokenExpiration());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UserPrincipal extractUserPrincipal(String token) {
        Claims claims = extractAllClaims(token);

        UUID userId = UUID.fromString(claims.get("id").toString());

        return new UserPrincipal(
                userId,
                (String) claims.get("nickname"),
                claims.getSubject(),
                (String) claims.get("warehouseId"),
                extractAuthorities(claims)
        );
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (BadCredentialsException | TokenExpiredException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signInKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token expired", "token expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid or expired JWT token", e);
        }
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<?> authorities = claims.get("authorities", List.class);
        if (authorities == null) {
            throw new BadCredentialsException("Invalid or expired JWT token");
        }
        return authorities.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(signInKey)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}