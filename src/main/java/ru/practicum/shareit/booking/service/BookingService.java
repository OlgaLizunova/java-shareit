package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;

import java.util.List;

public interface BookingService {

    BookingOutputDto addBooking(BookingInputDto bookingInputDto, long bookerId);

    BookingOutputDto updateBooking(long bookingId, long ownerId, boolean isApproved);

    BookingOutputDto getBookingByIdAndBookerId(long bookingId, long userId);

    List<BookingOutputDto> getAllUsersBookings(Long bookerId, String stateParam, int from, int size);

    List<BookingOutputDto> getAllOwnersBookings(Long ownerId, String stateParam, int from, int size);
}
