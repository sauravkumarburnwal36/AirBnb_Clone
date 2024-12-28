package com.explorebnb.clone.airBnbApp.repository;

import com.explorebnb.clone.airBnbApp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest,Long> {
}
