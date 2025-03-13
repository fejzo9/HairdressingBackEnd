package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findBySalonId(Long salonId);
}
