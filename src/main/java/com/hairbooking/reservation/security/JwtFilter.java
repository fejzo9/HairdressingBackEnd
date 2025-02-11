package com.hairbooking.reservation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
 * provjerava JWT token u svakom HTTP zahtjevu i autentifikuje korisnika.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        System.out.println("🔍 Request URI: " + request.getRequestURI());
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 🟢 Ako je zahtjev za profilnu sliku, pusti ga bez autentifikacije
        if (request.getMethod().equals("GET") && request.getRequestURI().matches("^/users/\\d+/profile-picture$")) {
            System.out.println("✅ Profilna slika - JWT filter preskače autentifikaciju");
            chain.doFilter(request, response);
            return;
        }

        // 1️ Provjerava da li zahtjev sadrži "Authorization" header sa JWT tokenom
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 2️⃣ Ekstraktuje token iz Authorization headera
        final String token = authorizationHeader.substring(7);
        final String username = jwtUtil.extractUsername(token);

        // 3️⃣ Ako korisnik nije autentifikovan i token je validan, dopusti pristup
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(token, username)) {
                String role = jwtUtil.extractRole(token);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null,
                                List.of(new SimpleGrantedAuthority(role))); // ✅ Postavlja ulogu korisnika

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        System.out.println("🔒 Proslijeđen zahtjev dalje u filter chain");
        // 4️⃣ Proslijedi zahtjev dalje u filter chain
        chain.doFilter(request, response);
    }

}
