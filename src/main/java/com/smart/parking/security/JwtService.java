package com.smart.parking.security;

import com.smart.parking.config.AppProperties;
import com.smart.parking.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final AppProperties props;

    public JwtService(AppProperties props) { this.props = props; }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getJwt().getExpirationMs()))
                .signWith(signingKey());

        if (user.getManagedPlace() != null) {
            builder.claim("placeId", user.getManagedPlace().getId());
        }
        if (user.getManagedState() != null) {
            builder.claim("stateId", user.getManagedState().getId());
        }
        return builder.compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        try {
            return extractEmail(token).equals(email)
                    && !extractAllClaims(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getJwt().getSecret()));
    }
}
