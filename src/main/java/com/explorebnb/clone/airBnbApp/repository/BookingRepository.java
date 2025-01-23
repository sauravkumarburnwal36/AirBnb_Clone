package com.explorebnb.clone.airBnbApp.repository;

import com.explorebnb.clone.airBnbApp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);
}
