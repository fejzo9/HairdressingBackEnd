package com.hairbooking.reservation.config;

import com.hairbooking.reservation.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
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

                            // Endpoint za dohvatanje i dodavanje profilne slike sa /users endpointa
                            .requestMatchers(HttpMethod.GET, "/users/*/profile-picture").permitAll()
                            .requestMatchers(HttpMethod.GET, "/users/*").permitAll()
                            .requestMatchers(HttpMethod.GET, "/users/username/*").permitAll()
                            .requestMatchers(HttpMethod.GET, "/users/role/*").hasAnyRole("ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.POST, "/users/*/upload-profile-picture").hasAnyRole("USER", "OWNER", "HAIRDRESSER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/users/**").permitAll()

                            // Endpoint za promjenu passworda korisnika
                            .requestMatchers(HttpMethod.POST,"/users/change-password").hasAnyRole("USER", "HAIRDRESSER", "OWNER", "ADMIN", "SUPER_ADMIN")

                            // 🔓 Endpointi dostupni svima (registracija i login)
                            .requestMatchers("/registration/**", "/login/**").permitAll()

                            // 👤 Endpointi dostupni samo korisnicima sa ulogom USER
                            .requestMatchers("/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                            // 🏛️ Endpointi dostupni samo SUPER ADMINIMA
                            .requestMatchers("/admins/**").hasRole("SUPER_ADMIN")

                            // ✂️ Endpointi dostupni samo FRIZERIMA
                            .requestMatchers("/hairdressers/**").hasRole("HAIRDRESSER")

                            // Endpointi dostupni samo VLASNICIMA salona
                            .requestMatchers("/owners/**").hasRole("OWNER")

                            // Endpoint /salons i pristup metodama
                            .requestMatchers(HttpMethod.GET, "/salons/{id}/images").permitAll()
                            .requestMatchers(HttpMethod.GET, "/salons/{id}/images/{imageIndex}").permitAll()
                            .requestMatchers(HttpMethod.GET, "/salons/owner").hasRole("OWNER")
                            .requestMatchers(HttpMethod.GET, "/salons/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/salons/{id}/upload-images").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.POST, "/salons/**").hasAnyRole( "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/salons/{salonId}/images/{imageIndex}").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/salons/**").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/salons/{salonId}/employees/{employeeId}").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/salons/{salonId}/images/{imageIndex}").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/salons/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                            // Endpoint /service i pristup metodama
                            .requestMatchers(HttpMethod.GET, "/services/salon/{salonId}").permitAll() // Svi mogu vidjeti usluge
                            .requestMatchers(HttpMethod.POST, "/services/salon/{salonId}").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/services/salon/{salonId}/{serviceId}").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.DELETE,  "/services/salon/{salonId}/{serviceId}").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")

                            // Endpoints for calendar
                            .requestMatchers(HttpMethod.GET, "/calendars/hairdresser/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/calendars/hairdresser/**").hasAnyRole("HAIRDRESSER", "OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/calendars/hairdresser/**").hasAnyRole("HAIRDRESSER", "OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/calendars/hairdresser/**").hasAnyRole("HAIRDRESSER", "OWNER", "ADMIN", "SUPER_ADMIN")

                            // Endpoints for appointment
                            .requestMatchers(HttpMethod.POST, "/appointments/book").hasAnyRole("USER", "HAIRDRESSER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/appointments/calendar/**").hasAnyRole("USER", "HAIRDRESSER", "OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/appointments/**").hasAnyRole("USER", "HAIRDRESSER", "OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/appointments/**").hasAnyRole("HAIRDRESSER", "OWNER", "ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/appointments/**").hasAnyRole("HAIRDRESSER", "OWNER", "ADMIN", "SUPER_ADMIN")

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
