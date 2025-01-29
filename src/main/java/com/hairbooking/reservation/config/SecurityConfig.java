package com.hairbooking.reservation.config;

import com.hairbooking.reservation.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable) // Privremeno isključivanje CSRF zaštite za testiranje
                    .cors(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT - Bez sesija
                    .authorizeHttpRequests(auth -> auth
                            // 🔓 Endpointi dostupni svima (registracija i login)
                            .requestMatchers("/registration/**", "/login/**").permitAll()

                            // 👤 Endpointi dostupni samo korisnicima sa ulogom USER
                            .requestMatchers("/users/**").hasRole("USER")

                            // 🏛️ Endpointi dostupni samo ADMINIMA
                            .requestMatchers("/admins/**").hasRole("ADMIN")

                            // ✂️ Endpointi dostupni samo FRIZERIMA
                            .requestMatchers("/hairdressers/**").hasRole("HAIRDRESSER")

                            // Endpointi dostupni samo VLASNICIMA salona
                            .requestMatchers("/owners/**").hasRole("OWNER")

                            // 🚫 Svi ostali zahtjevi zahtijevaju autentifikaciju
                            .anyRequest().authenticated()
                )
                    // Dodajemo JWT filter ispred defaultnog filtera za autentifikaciju
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
