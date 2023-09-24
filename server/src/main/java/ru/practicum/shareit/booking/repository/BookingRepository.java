package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_Id(Long bookerId, Pageable pageable);

    List<Booking> findAllByBooker_IdAndEndBefore(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByBooker_IdAndStartAfter(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByBooker_IdAndStartBeforeAndEndAfter(Long bookerId,
                                                              LocalDateTime now1,
                                                              LocalDateTime now2,
                                                              Pageable pageable);

    List<Booking> findAllByBooker_IdAndStatus(Long bookerId, Status status, Pageable pageable);

    List<Booking> findAllByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByItem_Owner_IdAndStartBeforeAndEndAfter(Long ownerId,
                                                                  LocalDateTime now1,
                                                                  LocalDateTime now2,
                                                                  Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, Status status, Pageable pageable);

    @Query(value = "SELECT DISTINCT ON(item_id) * FROM bookings b " +
            "WHERE b.item_id IN :itemIds " +
            "AND b.status = :status " +
            "AND b.start_date < :date " +
            "ORDER BY item_id, b.start_date DESC", nativeQuery = true)
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("status") String status,
                                           @Param("date") LocalDateTime date);

    @Query(value = "SELECT DISTINCT ON(item_id) * FROM bookings b " +
            "WHERE b.item_id IN :itemIds " +
            "AND b.status = :status " +
            "AND b.start_date >= :date " +
            "ORDER BY item_id, b.start_date ASC ;", nativeQuery = true)
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("status") String status,
                                           @Param("date") LocalDateTime date);

    Booking findFirstByItem_IdAndBooker_IdAndEndBefore(Long itemId, Long bookerId, LocalDateTime date);

    Optional<Booking> findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(Long itemId,
                                                                           LocalDateTime after,
                                                                           LocalDateTime before,
                                                                           Status status);
}
