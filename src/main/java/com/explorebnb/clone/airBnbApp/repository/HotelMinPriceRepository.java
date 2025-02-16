package com.explorebnb.clone.airBnbApp.repository;

import com.explorebnb.clone.airBnbApp.dto.HotelPriceDto;
import com.explorebnb.clone.airBnbApp.entity.Hotel;
import com.explorebnb.clone.airBnbApp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.time.LocalDate;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice,Long> {
    @Query("""
            SELECT new com.explorebnb.clone.airBnbApp.dto.HotelPriceDto(i.hotel,AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
//                AND (:roomCounts IS NULL OR 1 = 1)
//                AND (:dateCount IS NULL OR 1 = 1)
           GROUP BY i.hotel
           """)
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCounts") Integer roomCounts,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
