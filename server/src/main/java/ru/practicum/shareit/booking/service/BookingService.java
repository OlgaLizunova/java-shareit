package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.controller.State;

import java.util.List;

public interface BookingService {

    BookingOutputDto addBooking(BookingInputDto bookingInputDto, long bookerId);

    BookingOutputDto updateBooking(long bookingId, long ownerId, boolean isApproved);

    BookingOutputDto getBookingByIdAndBookerId(long bookingId, long userId);

    List<BookingOutputDto> getAllUsersBookings(Long bookerId, State state, int from, int size);

    List<BookingOutputDto> getAllOwnersBookings(Long ownerId, State state, int from, int size);
}
