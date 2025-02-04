package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalonRepository extends JpaRepository<Salon, Long> {
    List<Salon> findByOwnerId(Long ownerId);
}
