package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    @EntityGraph(attributePaths = {"calendar"})
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByRole(Role role);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.calendar WHERE u.username = :username")
    Optional<User> findByUsernameWithCalendar(@Param("username") String username);

}
