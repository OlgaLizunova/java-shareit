package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.State;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String HEADER = "X-Sharer-User-Id";
    private static final String DEFAULT_SIZE = "25";
    private static final String DEFAULT_FROM = "0";

    @PostMapping()
    public ResponseEntity<Object> addBooking(@RequestHeader(value = HEADER) long bookerId,
                                             @Valid @RequestBody BookingInputDto bookingInputDto) {
        log.info("получен POST запрос на добавление нового бронирования с bookerId={}, body={}",
                bookerId,
                bookingInputDto);
        return bookingClient.addBooking(bookerId, bookingInputDto);
    }

    @PatchMapping("/{bookingId}")
    ResponseEntity<Object> patchBooking(@RequestHeader(value = HEADER) long ownerId,
                                        @PathVariable(value = "bookingId") long bookingId,
                                        @RequestParam(value = "approved") boolean isApproved) {
        return bookingClient.updateBooking(bookingId, ownerId, isApproved);
    }

    @GetMapping("/{bookingId}")
    ResponseEntity<Object> getBooking(@RequestHeader(value = HEADER) long userId,
                                      @PathVariable(value = "bookingId") long bookingId) {
        log.info("получен GET запрос на просмотр бронирования с id={}, userId={}", bookingId, userId);

        return bookingClient.getBooking(bookingId, userId);
    }

    @GetMapping("")
    ResponseEntity<Object> getAllUsersBooking(@RequestHeader(HEADER) long bookerId,
                                              @RequestParam(name = "state",
                                                      defaultValue = "ALL") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                              Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10")
                                              Integer size) {
        State state = State.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("получен GET запрос на просмотр всех бронирований для bookerId={}, state={}, from={}, size={}",
                bookerId, state, from, size);
        return bookingClient.getAllUsersBookings(bookerId, state, from, size);
    }


    @GetMapping("/owner")
    ResponseEntity<Object> getAllOwnersBooking(@RequestHeader(value = HEADER) long ownerId,
                                               @RequestParam(value = "state",
                                                       defaultValue = "ALL") String stateParam,
                                               @RequestParam(value = "from",
                                                       defaultValue = DEFAULT_FROM) @PositiveOrZero int from,
                                               @RequestParam(value = "size",
                                                       defaultValue = DEFAULT_SIZE) @Positive int size) {
        log.info("получен GET запрос на просмотр всех бронирований для owner={}, state={}, from={}, size={}",
                ownerId, stateParam, from, size);
        State state = State.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));

        return bookingClient.getAllOwnersBookings(ownerId, state, from, size);
    }

    private void validateBookingData(BookingInputDto bookingInputDto) {
        if (!bookingInputDto.getEnd().isAfter(bookingInputDto.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: end date must be after start date");
        }
    }
}
