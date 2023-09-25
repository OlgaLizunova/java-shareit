package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.controller.State;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotAvailableException;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.utils.PageRequestUtil;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public BookingOutputDto addBooking(BookingInputDto bookingInputDto, long bookerId) {
        validateBookingData(bookingInputDto);
        Item item = findItemById(bookingInputDto.getItemId());
        if (!item.getAvailable()) {
            log.error("Вещь с id={} недоступна", item.getId());
            throw new NotAvailableException(
                    String.format("Вещь с id=%d недоступна", item.getId()));
        }
        if (isAlreadyBooked(bookingInputDto)) {
            log.error("Вещь с id={} недоступна", item.getId());
            throw new NotAvailableException(
                    String.format("Вещь с id=%d недоступна", item.getId()));
        }
        if (item.getOwner().getId() == (bookerId)) {
            log.error("bookerId={} равен ownerId для вещи с id={} ", bookerId, item.getId());
            throw new ElementNotFoundException(
                    String.format("bookerId=%d равен ownerId для вещи с id=%d", bookerId, item.getId()));
        }
        User booker = findUserById(bookerId);
        Booking newBooking = bookingMapper.toBooking(bookingInputDto, item, booker);
        Booking addedBooking = bookingRepository.save(newBooking);
        log.info("было добавлено booking={}", addedBooking);
        return bookingMapper.toBookingOutputDto(addedBooking);
    }

    @Transactional
    @Override
    public BookingOutputDto updateBooking(long bookingId, long ownerId, boolean isApproved) {
        findUserById(ownerId);
        Booking booking = findBookingById(bookingId);
        if (booking.getItem().getOwner().getId() != ownerId) {
            log.error("Только владелец имеет доступ к вещи");
            throw new ElementNotFoundException("Только владелец имеет доступ к вещи");
        }
        if (booking.getStatus() != Status.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("бронирование с bookingId=%d не в статусе WAITING", bookingId));
        }
        Status newStatus = isApproved ? Status.APPROVED : Status.REJECTED;
        booking.setStatus(newStatus);

        return bookingMapper.toBookingOutputDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingOutputDto getBookingByIdAndBookerId(long bookingId, long userId) {
        findUserById(userId);
        Booking booking = findBookingByIdAndUserId(bookingId, userId);
        log.info("Показано бронирование booking={}, для id={}, userId={}", booking, bookingId, userId);
        return bookingMapper.toBookingOutputDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutputDto> getAllUsersBookings(Long bookerId, State state, int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException(String.format("illegal from: %s", from));
        }
        findUserById(bookerId);
        List<Booking> allUsersBookings = new ArrayList<>();
        Pageable sortedByStart = PageRequestUtil.of(from, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                allUsersBookings = bookingRepository.findAllByBooker_Id(bookerId, sortedByStart);
                break;
            case PAST:
                allUsersBookings = bookingRepository.findAllByBooker_IdAndEndBefore(bookerId, now, sortedByStart);
                break;
            case FUTURE:
                allUsersBookings = bookingRepository.findAllByBooker_IdAndStartAfter(bookerId, now, sortedByStart);
                break;
            case CURRENT:
                allUsersBookings = bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfter(
                        bookerId, now, now, sortedByStart);
                break;
            case WAITING:
                allUsersBookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId, Status.WAITING, sortedByStart);
                break;
            case REJECTED:
                allUsersBookings =
                        bookingRepository.findAllByBooker_IdAndStatus(bookerId, Status.REJECTED, sortedByStart);
                break;
            default:
                break;
        }
        List<BookingOutputDto> allBookingsDto = bookingMapper.map(allUsersBookings);
        log.info("Покзаны все {} бронирования для bookerId={}", allBookingsDto.size(), bookerId);
        return allBookingsDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutputDto> getAllOwnersBookings(Long ownerId, State state, int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException(String.format("illegal from: %s", from));
        }
        findUserById(ownerId);
        List<Booking> allUsersBookings = new ArrayList<>();
        Pageable sortedByStart = PageRequestUtil.of(from, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                allUsersBookings = bookingRepository.findAllByItemOwnerId(ownerId, sortedByStart);
                break;
            case PAST:
                allUsersBookings = bookingRepository.findAllByItemOwnerIdAndEndBefore(ownerId, now, sortedByStart);
                break;
            case FUTURE:
                allUsersBookings =
                        bookingRepository.findAllByItemOwnerIdAndStartAfter(ownerId, now, sortedByStart);
                break;
            case CURRENT:
                allUsersBookings = bookingRepository.findAllByItem_Owner_IdAndStartBeforeAndEndAfter(
                        ownerId, now, now, PageRequestUtil.of(from, size, Sort.by(Sort.Direction.DESC, "id"))
                );
                break;
            case WAITING:
                allUsersBookings =
                        bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, Status.WAITING, sortedByStart);
                break;
            case REJECTED:
                allUsersBookings =
                        bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, Status.REJECTED, sortedByStart);
                break;
            default:
                break;
        }
        List<BookingOutputDto> allBookingsDto = bookingMapper.map(allUsersBookings);
        log.info("Показаны все {} бронирования для ownerId={}", allBookingsDto.size(), ownerId);
        return allBookingsDto;
    }

    private Booking findBookingByIdAndUserId(Long bookingId, Long userId) {
        Booking booking = findBookingById(bookingId);
        if (booking.getBooker().getId().equals(userId) ||
                booking.getItem().getOwner().getId().equals(userId)) {
            return booking;
        }
        throw new ElementNotFoundException(String.format("бронирование id=%d с (bookerId or ownerId)=%d не найдено",
                bookingId,
                userId));
    }

    private void validateBookingData(BookingInputDto bookingInputDto) {
        if (!bookingInputDto.getEnd().isAfter(bookingInputDto.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "дата окончания должна быть позднее даты начала");
        }
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new ElementNotFoundException(String.format("пользователь с id=%d не найден", userId)));
    }

    private Item findItemById(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new ElementNotFoundException(String.format("вещь с id=%d не найдена", itemId)));
    }

    private Booking findBookingById(long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new ElementNotFoundException(String.format("бронирование с id=%d не найдено", bookingId)));
    }


    private boolean isAlreadyBooked(BookingInputDto bookingInputDto) {
        Optional<Booking> doubleBooking = bookingRepository.findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(
                bookingInputDto.getItemId(),
                bookingInputDto.getStart(),
                bookingInputDto.getEnd(),
                Status.APPROVED);
        return doubleBooking.isPresent();
    }

}
