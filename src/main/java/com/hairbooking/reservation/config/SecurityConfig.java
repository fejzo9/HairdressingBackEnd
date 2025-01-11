package com.hairbooking.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable) // Privremeno isključivanje CSRF zaštite za testiranje
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/registration/**", "/users/**", "/login/**").permitAll() // Omogućiti pristup za POST na /users i svim podputanjama
                        .anyRequest().authenticated() // Svi ostali zahtjevi zahtijevaju autentifikaciju
                ).cors(Customizer.withDefaults());
        return http.build();
    }

}
