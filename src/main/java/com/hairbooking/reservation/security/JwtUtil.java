package com.hairbooking.reservation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/*
 * Kreira i provjerava JWT tokene
 */
@Component
public class JwtUtil {

    // Tajni ključ u Base64 formatu (mora biti najmanje 256-bitni ključ)
    private static final String SECRET_KEY = "superSecretKeysuperSecretKeysuperSecretKey123";

    // Konvertovanje ključa u SecretKey objekt
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ 1. Generisanje JWT tokena
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)  // Postavlja korisničko ime
                .claim("role", role) // Postavlja korisničku ulogu
                .issuedAt(new Date()) // Vrijeme izdavanja tokena
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 sati važenja
                .signWith(getSignKey()) // Potpisivanje novim metodama
                .compact();
    }

    // ✅ 2. Ekstrakcija podataka (claimova) iz tokena
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey()) // Provjera potpisa
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ✅ 3. Dohvatanje korisničkog imena iz tokena
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // ✅ 4. Dohvatanje korisničke uloge iz tokena
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // ✅ 5. Validacija tokena
    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) // Provjera da li token pripada korisniku
                && extractClaims(token).getExpiration().after(new Date()); // Provjera da nije istekao
    }
}

