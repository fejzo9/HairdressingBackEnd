package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsernameOrEmail(String username, String email);
}


