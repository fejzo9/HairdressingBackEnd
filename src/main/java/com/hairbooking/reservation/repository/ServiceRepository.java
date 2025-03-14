package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.ServiceInSalon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceInSalon, Long> {
    List<ServiceInSalon> findBySalonId(Long salonId);
}
