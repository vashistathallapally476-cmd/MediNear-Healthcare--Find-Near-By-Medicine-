package com.mednear.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT utility — generate, parse, validate tokens.
 *
 * Key points:
 *  • jwtSecret in application.properties MUST be Base64-encoded
 *    (at least 32 bytes / 256 bits for HS256).
 *  • validateJwtToken catches individual JwtException subtypes so the
 *    log message tells you exactly WHAT went wrong.
 *  • generateJwtToken embeds email as subject (not user ID) for
 *    easy loadUserByUsername lookup in AuthTokenFilter.
 */
@Component
public class JwtUtils {

    @Value("${mednear.app.jwtSecret}")
    private String jwtSecret;

    @Value("${mednear.app.jwtExpirationMs}")
    private long jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
            .setSubject(principal.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(signingKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log("Malformed JWT", e);
        } catch (ExpiredJwtException e) {
            log("Expired JWT", e);
        } catch (UnsupportedJwtException e) {
            log("Unsupported JWT", e);
        } catch (IllegalArgumentException e) {
            log("Empty JWT claims", e);
        }
        return false;
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private void log(String context, Exception e) {
        System.err.println("[JwtUtils] " + context + ": " + e.getMessage());
    }
}
