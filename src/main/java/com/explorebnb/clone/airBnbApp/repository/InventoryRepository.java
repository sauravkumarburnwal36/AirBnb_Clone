package com.explorebnb.clone.airBnbApp.repository;

import com.explorebnb.clone.airBnbApp.dto.RoomPriceDto;
import com.explorebnb.clone.airBnbApp.entity.Hotel;
import com.explorebnb.clone.airBnbApp.entity.Inventory;
import com.explorebnb.clone.airBnbApp.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    void deleteByRoom(Room room);

    @Query("""
            SELECT DISTINCT i.hotel
            FROM Inventory i
            WHERE i.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed = false
                AND (i.totalCount - i.bookedCount-i.reservedCount) >= :roomCounts
           GROUP BY i.hotel, i.room
           HAVING COUNT(i.date) = :dateCount
           """)
    Page<Hotel> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCounts") Integer roomCounts,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );


    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id=:roomId
            AND i.closed=false
            AND i.date BETWEEN :startDate and :endDate
            AND (i.totalCount-i.bookedCount-i.reservedCount)>=:roomCounts
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCounts") Integer roomCounts);

    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);

    List<Inventory> findByRoomOrderByDate(Room room);
    @Query("""
            SELECT i
            FROM Inventory i
            where i.room.id=:roomId
            AND i.closed=false
            AND i.date BETWEEN :startDate and :endDate
            AND (i.totalCount-i.bookedCount)>=:numberOfRooms
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockReservedInventory(@Param("roomId") Long roomId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("numberOfRooms") Integer numberOfRooms);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.reservedCount=i.reservedCount-:numberOfRooms,
            i.bookedCount=i.bookedCount+:numberOfRooms
            where i.room.id=:roomId
            AND i.date BETWEEN :startDate and :endDate
            AND (i.totalCount-i.bookedCount)>=:numberOfRooms
            AND i.reservedCount>=:numberOfRooms
            AND i.closed=false
            """)
    void confirmBooking(
            @Param("roomId") Long roomId,@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,@Param("numberOfRooms") Integer numberOfRooms);


    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.reservedCount= i.reservedCount+:numberOfRooms
            where i.room.id=:roomId
            AND i.date BETWEEN :startDate and :endDate
            AND (i.totalCount - i.bookedCount -i.reservedCount)>=:numberOfRooms
            AND i.closed=false
            """)
    void initBooking(@Param("roomId")Long roomId,@Param("startDate") LocalDate startDate,
                     @Param("endDate")LocalDate endDate,@Param("numberOfRooms")Integer numberOfRooms);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.bookedCount=i.bookedCount-:numberOfRooms
            where i.room.id=:roomId
            AND i.date BETWEEN :startDate and :endDate
            AND (i.totalCount-i.bookedCount)>=:numberOfRooms
            AND i.closed=false
            """)
    void cancelBooking(@Param("roomId") Long roomId,@Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate,@Param("numberOfRooms") Integer numberOfRooms);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.surgeFactor=:surgeFactor,
            i.closed=:closed
            where i.room.id=:roomId
            AND i.date BETWEEN :startDate and :endDate
            """)
    void updateInventory(@Param("roomId")Long roomId, @Param("startDate") LocalDate startDate
    , @Param("endDate") LocalDate endDate, @Param("surgeFactor")BigDecimal surgeFactor,
                       @Param("closed") Boolean closed);


    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id=:roomId
            AND i.date BETWEEN :startDate and :endDate
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    void findInventoryAndLockBeforeUpdate(@Param("roomId") Long roomId,
                                         @Param("startDate")LocalDate startDate,
                                         @Param("endDate")LocalDate endDate);

    @Query("""
       SELECT new com.explorebnb.clone.airBnbApp.dto.RoomPriceDto(
            i.room,
            CASE
                WHEN COUNT(i) = :dateCount THEN AVG(i.price)
                ELSE NULL
            END
        )
       FROM Inventory i
       WHERE i.hotel.id = :hotelId
             AND i.date BETWEEN :startDate AND :endDate
             AND (i.totalCount - i.bookedCount) >= :roomsCount
             AND i.closed = false
       GROUP BY i.room
       """)
    List<RoomPriceDto> findRoomAveragePrice(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Long roomsCount,
            @Param("dateCount") Long dateCount
    );

}
