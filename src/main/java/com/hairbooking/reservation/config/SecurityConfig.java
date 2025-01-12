package com.hairbooking.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable) // Privremeno isključivanje CSRF zaštite za testiranje
                    .cors(Customizer.withDefaults())
                    .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/registration/**", "/users/**", "/login/**", "/admins/**").permitAll() // Omogućiti pristup za POST na /users i svim podputanjama
                      //  .requestMatchers("/admins/**").hasRole("ADMIN")
                        .anyRequest().authenticated() // Svi ostali zahtjevi zahtijevaju autentifikaciju
                ).cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
