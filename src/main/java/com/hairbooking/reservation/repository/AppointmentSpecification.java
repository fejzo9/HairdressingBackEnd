package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.Appointment;
import com.hairbooking.reservation.model.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AppointmentSpecification {

    public static Specification<Appointment> hasCustomerId(Long customerId) {
        return (root, query, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Appointment> isUpcoming(LocalDate today) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), today);
    }

    public static Specification<Appointment> isPast(LocalDate today) {
        return (root, query, cb) -> cb.and(
                cb.lessThan(root.get("date"), today),
                cb.notEqual(root.get("status"), AppointmentStatus.CANCELLED)
        );
    }

    public static Specification<Appointment> isCancelled() {
        return (root, query, cb) -> cb.equal(root.get("status"), AppointmentStatus.CANCELLED);
    }
}
