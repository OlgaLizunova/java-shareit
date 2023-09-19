package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String HEADER = "X-Sharer-User-Id";

    @PostMapping()
    public BookingOutputDto addBooking(@RequestHeader(value = HEADER) long bookerId,
                                       @Valid @RequestBody BookingInputDto bookingInputDto) {
        log.info("получен POST запрос на добавление нового бронирования с bookerId={}, body={}",
                bookerId,
                bookingInputDto);
        return bookingService.addBooking(bookingInputDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    BookingOutputDto patchBooking(@RequestHeader(value = HEADER) long ownerId,
                                  @PathVariable(value = "bookingId") long bookingId,
                                  @RequestParam(value = "approved") boolean isApproved) {
        return bookingService.updateBooking(bookingId, ownerId, isApproved);
    }

    @GetMapping("/{bookingId}")
    BookingOutputDto getBooking(@RequestHeader(value = HEADER) long userId,
                                @PathVariable(value = "bookingId") long bookingId) {
        log.info("получен GET запрос на просмотр бронирования с id={}, userId={}", bookingId, userId);

        return bookingService.getBookingByIdAndBookerId(bookingId, userId);
    }

    @GetMapping()
    List<BookingOutputDto> getAllUsersBooking(@RequestHeader(value = HEADER) long bookerId,
                                              @RequestParam(value = "state",
                                                      defaultValue = "ALL") String stateParam) {

        log.info("получен GET запрос на просмотр всех бронирований для bookerId={}, state={}", bookerId, stateParam);
        State state;
        try {
            state = State.valueOf(stateParam);
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: {}", stateParam);
            throw new IllegalArgumentException(String.format("Unknown state: %s", stateParam));
        }
        return bookingService.getAllUsersBookings(bookerId, state);
    }


    @GetMapping("/owner")
    List<BookingOutputDto> getAllOwnersBooking(@RequestHeader(value = HEADER) long ownerId,
                                               @RequestParam(value = "state",
                                                       defaultValue = "ALL") String stateParam) {
        log.info("получен GET на просмотр всех пронирований для owner={}, state={}", ownerId, stateParam);
        State state;
        try {
            state = State.valueOf(stateParam);
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: UNSUPPORTED_STATUS={}", stateParam);
            throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }

        return bookingService.getAllOwnersBookings(ownerId, state);
    }
}
