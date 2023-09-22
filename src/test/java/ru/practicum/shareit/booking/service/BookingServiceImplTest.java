package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotAvailableException;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @InjectMocks
    BookingServiceImpl bookingService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    BookingMapper bookingMapper;

    final LocalDateTime dateTime = LocalDateTime.of(2023, 8, 13, 9, 28, 26);
    final long userId = 1L;
    final String userName = "User";
    final String email = "User@mail.com";

    final long itemId = 1L;
    final String itemName = "itemName";
    final String itemDescription = "Item Description";

    final long bookingId = 1L;
    final LocalDateTime start = dateTime.minusDays(2);
    final LocalDateTime end = dateTime.minusDays(1);

    int from = 0;
    int size = 10;

    @Test
    void addBooking_whenItemIsAvailableAndNotBusyAndBookerNotOwner_thenBookingAdd() {
        Long ownerId = userId + 5;
        long bookerId = userId + 10;
        User owner = new User(ownerId, userName, email);
        User booker = new User(bookerId + 10, "booker", "booker@mail.ru");
        BookingInputDto bookingInputDto = new BookingInputDto(start, end, itemId);
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Booking newBooking = new Booking(
                0,
                bookingInputDto.getStart(),
                bookingInputDto.getEnd(),
                item,
                booker,
                Status.WAITING
        );
        Booking addedBooking = new Booking(
                bookerId,
                bookingInputDto.getStart(),
                bookingInputDto.getEnd(),
                item,
                booker,
                Status.WAITING
        );
        ItemDto itemDto = new ItemDto(item.getId(), itemName, itemDescription, item.getAvailable(), null);
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        BookingOutputDto expectedBookingDto = new BookingOutputDto(
                addedBooking.getId(),
                addedBooking.getStart(),
                addedBooking.getEnd(),
                itemDto,
                bookerDto,
                Status.APPROVED
        );

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingMapper.toBooking(bookingInputDto, item, booker)).thenReturn(newBooking);
        when(bookingRepository.save(newBooking)).thenReturn(addedBooking);
        when(bookingMapper.toBookingOutputDto(addedBooking)).thenReturn(expectedBookingDto);

        BookingOutputDto resultBookingOutputDto = bookingService.addBooking(bookingInputDto, bookerId);

        InOrder inOrder = inOrder(itemRepository, bookingRepository, userRepository, bookingMapper);
        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(bookingRepository).findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(
                anyLong(), any(), any(), any());
        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).save(newBooking);
        inOrder.verify(bookingMapper).toBookingOutputDto(addedBooking);
        assertEquals(expectedBookingDto, resultBookingOutputDto);
    }

    @Test
    void addBooking_whenBookingDataNotValid_thenNotAvailableException() {
        BookingInputDto bookingInputDto = new BookingInputDto(end, start, itemId);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,
                () -> bookingService.addBooking(bookingInputDto, userId));
        assertEquals(responseStatusException.getStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void addBooking_whenItemNotFound_thenNotFoundException() {
        BookingInputDto bookingInputDto = new BookingInputDto(start, end, itemId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.addBooking(bookingInputDto, userId));
    }

    @Test
    void addBooking_whenItemNotAvailable_thenNotAvailableException() {
        Long ownerId = userId + 10;
        User owner = new User(ownerId, userName, email);
        BookingInputDto bookingInputDto = new BookingInputDto(start, end, itemId);
        Item item = new Item(itemId, itemName, itemDescription, false, owner, null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(NotAvailableException.class,
                () -> bookingService.addBooking(bookingInputDto, userId));
    }

    @Test
    void addBooking_whenItemAlreadyBooked_thenNotAvailableException() {
        Long ownerId = userId + 10;
        User owner = new User(ownerId, userName, email);
        BookingInputDto bookingInputDto = new BookingInputDto(start, end, itemId);
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(new Booking()));

        assertThrows(NotAvailableException.class,
                () -> bookingService.addBooking(bookingInputDto, userId));
    }

    @Test
    void addBooking_whenBookerIsOwner_thenNotFoundException() {
        long ownerId = userId;
        User owner = new User(ownerId, userName, email);
        BookingInputDto bookingInputDto = new BookingInputDto(start, end, itemId);
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.addBooking(bookingInputDto, ownerId));
    }

    @Test
    void addBooking_whenBookerNotFound_thenNotFoundException() {
        Long ownerId = userId + 10;
        long bookerId = userId + 20;
        User owner = new User(ownerId, userName, email);
        BookingInputDto bookingInputDto = new BookingInputDto(start, end, itemId);
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.addBooking(bookingInputDto, bookerId));
    }

    @Test
    void updateBooking_whenIsApprovedIsTrue_thenBookingStatusUpdateToTrue() {
        Long ownerId = userId + 5;
        long bookerId = userId + 10;
        Status newStatus = Status.APPROVED;
        User owner = new User(ownerId, userName, email);
        User booker = new User(bookerId + 10, "booker", "booker@mail.ru");
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Booking oldBooking = new Booking(bookingId, start, end, item, booker, Status.WAITING);
        Booking updatedBooking = new Booking(
                oldBooking.getId(),
                oldBooking.getStart(),
                oldBooking.getEnd(),
                oldBooking.getItem(),
                oldBooking.getBooker(),
                newStatus
        );
        ItemDto itemDto = new ItemDto(item.getId(), itemName, itemDescription, item.getAvailable(), null);
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        BookingOutputDto updatedBookingDto = new BookingOutputDto(
                updatedBooking.getId(),
                updatedBooking.getStart(),
                updatedBooking.getEnd(),
                itemDto,
                bookerDto,
                Status.APPROVED
        );
        BookingOutputDto expectedBookingDto = new BookingOutputDto(
                bookingId,
                start,
                end,
                itemDto,
                bookerDto,
                Status.APPROVED);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(oldBooking));
        when(bookingRepository.save(updatedBooking)).thenReturn(updatedBooking);
        when(bookingMapper.toBookingOutputDto(updatedBooking)).thenReturn(updatedBookingDto);

        BookingOutputDto resultBookingOutputDto = bookingService.updateBooking(bookingId, ownerId, true);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository).findById(bookingId);
        inOrder.verify(bookingRepository).save(updatedBooking);
        inOrder.verify(bookingMapper).toBookingOutputDto(updatedBooking);
        assertEquals(expectedBookingDto, resultBookingOutputDto);
    }

    @Test
    void updateBooking_whenOwnerNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.updateBooking(bookingId, userId, true));
        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(userId);
    }

    @Test
    void updateBooking_whenBookingNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.updateBooking(bookingId, userId, true));
        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(userId);
        inOrder.verify(bookingRepository).findById(bookingId);
    }

    @Test
    void getBookingByIdAndBookerId_whenRequestFromBooker_thenBookingGet() {
        Long ownerId = userId + 5;
        long bookerId = userId + 10;
        User owner = new User(ownerId, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Booking booking = new Booking(bookingId, start, end, item, booker, Status.WAITING);
        ItemDto itemDto = new ItemDto(item.getId(), itemName, itemDescription, item.getAvailable(), null);
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        BookingOutputDto expectedBookingDto = new BookingOutputDto(
                bookingId,
                start,
                end,
                itemDto,
                bookerDto,
                Status.APPROVED
        );
        BookingOutputDto returnedBookingOutputDto = new BookingOutputDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemDto,
                bookerDto,
                Status.APPROVED
        );
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingOutputDto(booking)).thenReturn(returnedBookingOutputDto);

        BookingOutputDto resultBookingOutputDto = bookingService.getBookingByIdAndBookerId(bookingId, bookerId);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findById(bookingId);
        inOrder.verify(bookingMapper).toBookingOutputDto(booking);
        assertEquals(expectedBookingDto, resultBookingOutputDto);
    }

    @Test
    void getBookingByIdAndBookerId_whenRequestFromOwner_thenBookingGet() {
        Long ownerId = userId + 5;
        long bookerId = userId + 10;
        User owner = new User(ownerId, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Booking booking = new Booking(bookingId, start, end, item, booker, Status.WAITING);
        ItemDto itemDto = new ItemDto(item.getId(), itemName, itemDescription, item.getAvailable(), null);
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        BookingOutputDto expectedBookingDto = new BookingOutputDto(
                bookingId,
                start,
                end,
                itemDto,
                bookerDto,
                Status.APPROVED
        );
        BookingOutputDto returnedBookingOutputDto = new BookingOutputDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemDto,
                bookerDto,
                Status.APPROVED
        );
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingOutputDto(booking)).thenReturn(returnedBookingOutputDto);

        BookingOutputDto resultBookingOutputDto = bookingService.getBookingByIdAndBookerId(bookingId, ownerId);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository).findById(bookingId);
        inOrder.verify(bookingMapper).toBookingOutputDto(booking);
        assertEquals(expectedBookingDto, resultBookingOutputDto);
    }

    @Test
    void getBookingByIdAndBookerId_whenUserNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.getBookingByIdAndBookerId(bookingId, userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void getBookingByIdAndBookerId_whenBookingNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.getBookingByIdAndBookerId(bookingId, userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void getBookingByIdAndBookerId_whenUserNotBookerAndNotOwner_thenNotFoundException() {
        Long ownerId = userId + 5;
        long bookerId = userId + 10;
        User owner = new User(ownerId, userName, email);
        User user = new User(userId, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Booking booking = new Booking(bookingId, start, end, item, booker, Status.WAITING);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.getBookingByIdAndBookerId(bookingId, userId));
        verify(userRepository).findById(userId);
        verify(bookingRepository).findById(bookingId);
    }

    @Test
    void getAllUsersBookings_whenStateIsAll_thenReturnedAllThreeBookings() {
        long bookingId1 = bookingId + 10;
        long bookingId2 = bookingId + 11;
        long bookingId3 = bookingId + 12;
        Long ownerId1 = userId + 20;
        Long ownerId2 = userId + 21;
        Long ownerId3 = userId + 22;
        long bookerId = userId + 30;
        User owner1 = new User(ownerId1, userName, email);
        User owner2 = new User(ownerId2, userName, email);
        User owner3 = new User(ownerId3, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item2 = new Item(itemId + 2, itemName, itemDescription, true, owner2, null);
        Item item3 = new Item(itemId + 3, itemName, itemDescription, true, owner3, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto2 = new ItemDto(item2.getId(), itemName, itemDescription, item2.getAvailable(), null);
        ItemDto itemDto3 = new ItemDto(item3.getId(), itemName, itemDescription, item3.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start.plusHours(1), end.plusDays(1), item1, booker, Status.WAITING);
        Booking booking2 = new Booking(bookingId2, start.minusDays(1), end.minusHours(1), item2, booker, Status.APPROVED);
        Booking booking3 = new Booking(bookingId3, start, end, item3, booker, Status.CANCELED);
        List<Booking> allUsersBookings = List.of(booking1, booking2, booking3);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start.plusHours(1), end.plusDays(1), itemDto1, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto2 = new BookingOutputDto(
                bookingId2, start.minusDays(1), end.minusHours(1), itemDto2, bookerDto, Status.APPROVED);
        BookingOutputDto expectedBookingDto3 = new BookingOutputDto(
                bookingId3, start, end, itemDto3, bookerDto, Status.CANCELED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto2, expectedBookingDto3);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto2 = new BookingOutputDto(
                booking2.getId(), booking2.getStart(), booking2.getEnd(), itemDto2, bookerDto, booking2.getStatus());
        BookingOutputDto returnedBookingOutputDto3 = new BookingOutputDto(
                booking3.getId(), booking3.getStart(), booking3.getEnd(), itemDto3, bookerDto, booking3.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto2, returnedBookingOutputDto3);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_Id(eq(bookerId), any(PageRequest.class)))
                .thenReturn(allUsersBookings);
        when(bookingMapper.map(allUsersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllUsersBookings(bookerId, "all", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBooker_Id(eq(bookerId), any(PageRequest.class));
        inOrder.verify(bookingMapper).map(allUsersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllUsersBookings_whenStateIsPast_thenReturnedPastBookingOnly() {

        long bookingId2 = bookingId + 11;
        long bookingId3 = bookingId + 12;
        Long ownerId2 = userId + 21;
        Long ownerId3 = userId + 22;
        long bookerId = userId + 30;
        User owner2 = new User(ownerId2, userName, email);
        User owner3 = new User(ownerId3, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item2 = new Item(itemId + 2, itemName, itemDescription, true, owner2, null);
        Item item3 = new Item(itemId + 3, itemName, itemDescription, true, owner3, null);
        ItemDto itemDto2 = new ItemDto(item2.getId(), itemName, itemDescription, item2.getAvailable(), null);
        ItemDto itemDto3 = new ItemDto(item3.getId(), itemName, itemDescription, item3.getAvailable(), null);
        Booking booking2 =
                new Booking(bookingId2, start.minusDays(1), end.minusHours(1), item2, booker, Status.APPROVED);
        Booking booking3 = new Booking(bookingId3, start, end, item3, booker, Status.CANCELED);
        List<Booking> usersBookings = List.of(booking2, booking3);
        BookingOutputDto expectedBookingDto2 = new BookingOutputDto(
                bookingId2, start.minusDays(1), end.minusHours(1), itemDto2, bookerDto, Status.APPROVED);
        BookingOutputDto expectedBookingDto3 = new BookingOutputDto(
                bookingId3, start, end, itemDto3, bookerDto, Status.CANCELED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto2, expectedBookingDto3);
        BookingOutputDto returnedBookingOutputDto2 = new BookingOutputDto(
                booking2.getId(), booking2.getStart(), booking2.getEnd(), itemDto2, bookerDto, booking2.getStatus());
        BookingOutputDto returnedBookingOutputDto3 = new BookingOutputDto(
                booking3.getId(), booking3.getStart(), booking3.getEnd(), itemDto3, bookerDto, booking3.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto2, returnedBookingOutputDto3);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndEndBefore(
                eq(bookerId), any(LocalDateTime.class), any()))
                .thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllUsersBookings(bookerId, "past", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBooker_IdAndEndBefore(
                eq(bookerId), any(LocalDateTime.class), any());
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllUsersBookings_whenStateIsFuture_thenReturnedFutureBookingOnly() {
        long bookingId1 = bookingId + 10;
        long bookingId2 = bookingId + 11;

        Long ownerId1 = userId + 20;
        Long ownerId2 = userId + 21;
        long bookerId = userId + 30;
        LocalDateTime start1 = start.minusHours(1);
        LocalDateTime end1 = end.plusDays(1);
        LocalDateTime start2 = start.minusDays(1);
        LocalDateTime end2 = end.plusHours(1);

        User owner1 = new User(ownerId1, userName, email);
        User owner2 = new User(ownerId2, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item2 = new Item(itemId + 2, itemName, itemDescription, true, owner2, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto2 = new ItemDto(item2.getId(), itemName, itemDescription, item2.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start1, end1, item1, booker, Status.WAITING);
        Booking booking2 = new Booking(bookingId2, start2, end2, item2, booker, Status.APPROVED);
        List<Booking> usersBookings = List.of(booking1, booking2);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start1, end1, itemDto1, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto2 = new BookingOutputDto(
                bookingId2, start2, end2, itemDto2, bookerDto, Status.APPROVED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto2);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto2 = new BookingOutputDto(
                booking2.getId(), booking2.getStart(), booking2.getEnd(), itemDto2, bookerDto, booking2.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto2);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfter(
                eq(bookerId), any(LocalDateTime.class), any(LocalDateTime.class), any()))
                .thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllUsersBookings(bookerId, "current", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBooker_IdAndStartBeforeAndEndAfter(
                eq(bookerId), any(LocalDateTime.class), any(LocalDateTime.class), any());
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllUsersBookings_whenStateIsWaiting_thenReturnedWaitingBookingOnly() {
        long bookingId1 = bookingId + 10;
        long bookingId3 = bookingId + 12;
        Long ownerId1 = userId + 20;
        Long ownerId3 = userId + 22;
        long bookerId = userId + 30;
        LocalDateTime start1 = start.minusHours(1);
        LocalDateTime end1 = end.plusDays(1);
        LocalDateTime start3 = start;
        LocalDateTime end3 = end;
        User owner1 = new User(ownerId1, userName, email);
        User owner3 = new User(ownerId3, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item3 = new Item(itemId + 3, itemName, itemDescription, true, owner3, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto3 = new ItemDto(item3.getId(), itemName, itemDescription, item3.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start1, end1, item1, booker, Status.WAITING);
        Booking booking3 = new Booking(bookingId3, start3, end3, item3, booker, Status.WAITING);
        List<Booking> usersBookings = List.of(booking1, booking3);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start1, end1, itemDto1, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto3 = new BookingOutputDto(
                bookingId3, start3, end3, itemDto3, bookerDto, Status.WAITING);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto3);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto3 = new BookingOutputDto(
                booking3.getId(), booking3.getStart(), booking3.getEnd(), itemDto3, bookerDto, booking3.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto3);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStatus(
                eq(bookerId), eq(Status.WAITING), any(PageRequest.class)))
                .thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllUsersBookings(bookerId, "waiting", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBooker_IdAndStatus(
                eq(bookerId), eq(Status.WAITING), any(PageRequest.class));
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllUsersBookings_whenStateIsRejected_thenReturnedRejectedBookingOnly() {
        long bookingId1 = bookingId + 10;
        long bookingId2 = bookingId + 11;
        Long ownerId1 = userId + 20;
        Long ownerId2 = userId + 21;
        long bookerId = userId + 30;
        LocalDateTime start1 = start.minusHours(1);
        LocalDateTime end1 = end.plusDays(1);
        LocalDateTime start2 = start.minusDays(1);
        LocalDateTime end2 = end.plusHours(1);
        User owner1 = new User(ownerId1, userName, email);
        User owner2 = new User(ownerId2, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item2 = new Item(itemId + 2, itemName, itemDescription, true, owner2, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto2 = new ItemDto(item2.getId(), itemName, itemDescription, item2.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start1, end1, item1, booker, Status.REJECTED);
        Booking booking2 = new Booking(bookingId2, start2, end2, item2, booker, Status.REJECTED);
        List<Booking> usersBookings = List.of(booking1, booking2);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start1, end1, itemDto1, bookerDto, Status.REJECTED);
        BookingOutputDto expectedBookingDto2 = new BookingOutputDto(
                bookingId2, start2, end2, itemDto2, bookerDto, Status.REJECTED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto2);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto2 = new BookingOutputDto(
                booking2.getId(), booking2.getStart(), booking2.getEnd(), itemDto2, bookerDto, booking2.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto2);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStatus(
                eq(bookerId), eq(Status.REJECTED), any(PageRequest.class)))
                .thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllUsersBookings(bookerId, "rejected", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBooker_IdAndStatus(
                eq(bookerId), eq(Status.REJECTED), any(PageRequest.class));
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllUsersBookings_whenBookerNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.getAllUsersBookings(userId, "all", from, size));

        verify(userRepository).findById(userId);
    }

    @Test
    void getAllOwnersBookings_whenStateIsAll_thenReturnedAllOwnerBookings() {
        long bookingId1 = bookingId + 10;
        long bookingId2 = bookingId + 11;
        Long ownerId1 = userId + 20;
        long bookerId = userId + 30;
        User owner1 = new User(ownerId1, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item2 = new Item(itemId + 2, itemName, itemDescription, true, owner1, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto2 = new ItemDto(item2.getId(), itemName, itemDescription, item2.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start.plusHours(1), end.plusDays(1), item1, booker, Status.WAITING);
        Booking booking2 =
                new Booking(bookingId2, start.minusDays(1), end.minusHours(1), item2, booker, Status.APPROVED);
        List<Booking> allUsersBookings = List.of(booking1, booking2);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start.plusHours(1), end.plusDays(1), itemDto1, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto2 = new BookingOutputDto(
                bookingId2, start.minusDays(1), end.minusHours(1), itemDto2, bookerDto, Status.APPROVED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto2);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto2 = new BookingOutputDto(
                booking2.getId(), booking2.getStart(), booking2.getEnd(), itemDto2, bookerDto, booking2.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto2);
        when(userRepository.findById(ownerId1)).thenReturn(Optional.of(owner1));
        when(bookingRepository.findAllByItemOwnerId(
                ownerId1,
                PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(allUsersBookings);
        when(bookingMapper.map(allUsersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllOwnersBookings(ownerId1, "all", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(ownerId1);
        inOrder.verify(bookingRepository).findAllByItemOwnerId(
                ownerId1,
                PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "start")));
        inOrder.verify(bookingMapper).map(allUsersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllOwnersBookings_whenStateIsPast_thenReturnedFutureBookingOnly() {
        long bookingId1 = bookingId + 10;
        long bookingId3 = bookingId + 12;
        Long ownerId1 = userId + 20;

        long bookerId = userId + 30;
        LocalDateTime start1 = start.minusMonths(1);
        LocalDateTime end1 = end.minusDays(10);
        LocalDateTime start3 = start;
        LocalDateTime end3 = end;
        User owner1 = new User(ownerId1, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item3 = new Item(itemId + 3, itemName, itemDescription, true, owner1, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto3 = new ItemDto(item3.getId(), itemName, itemDescription, item3.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start1, end1, item1, booker, Status.WAITING);
        Booking booking3 = new Booking(bookingId3, start3, end3, item3, booker, Status.CANCELED);
        List<Booking> usersBookings = List.of(booking1, booking3);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start1, end1, itemDto1, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto3 = new BookingOutputDto(
                bookingId3, start3, end3, itemDto3, bookerDto, Status.CANCELED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto3);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto3 = new BookingOutputDto(
                booking3.getId(), booking3.getStart(), booking3.getEnd(), itemDto3, bookerDto, booking3.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto3);
        when(userRepository.findById(ownerId1)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByItemOwnerIdAndEndBefore(eq(ownerId1), any(LocalDateTime.class), any()))
                .thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllOwnersBookings(ownerId1, "past", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(ownerId1);
        inOrder.verify(bookingRepository).findAllByItemOwnerIdAndEndBefore(
                eq(ownerId1), any(LocalDateTime.class), any());
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllOwnersBookings_whenStateIsFuture_thenReturnedFutureBookingOnly() {
        long bookingId1 = bookingId + 10;
        long bookingId3 = bookingId + 12;
        Long ownerId1 = userId + 20;
        Long ownerId3 = userId + 22;
        long bookerId = userId + 30;
        LocalDateTime start1 = start.plusHours(1);
        LocalDateTime end1 = end.plusDays(1);
        LocalDateTime start3 = start.plusMonths(1);
        LocalDateTime end3 = end.plusMonths(2);
        User owner1 = new User(ownerId1, userName, email);
        User owner3 = new User(ownerId3, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item3 = new Item(itemId + 3, itemName, itemDescription, true, owner3, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto3 = new ItemDto(item3.getId(), itemName, itemDescription, item3.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start1, end1, item1, booker, Status.WAITING);
        Booking booking3 = new Booking(bookingId3, start3, end3, item3, booker, Status.CANCELED);
        List<Booking> usersBookings = List.of(booking1, booking3);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start1, end1, itemDto1, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto3 = new BookingOutputDto(
                bookingId3, start3, end3, itemDto3, bookerDto, Status.CANCELED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto3);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto3 = new BookingOutputDto(
                booking3.getId(), booking3.getStart(), booking3.getEnd(), itemDto3, bookerDto, booking3.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto3);
        when(userRepository.findById(ownerId1)).thenReturn(Optional.of(owner1));
        when(bookingRepository.findAllByItemOwnerIdAndStartAfter(eq(ownerId1), any(LocalDateTime.class), any()))
                .thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllOwnersBookings(ownerId1, "future", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(ownerId1);
        inOrder.verify(bookingRepository).findAllByItemOwnerIdAndStartAfter(
                eq(ownerId1), any(LocalDateTime.class), any());
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllOwnersBookings_whenStateIsCurrent_thenReturnedCurrentBookingOnly() {
        long bookingId1 = bookingId + 10;
        long bookingId2 = bookingId + 11;
        Long ownerId1 = userId + 20;
        Long ownerId2 = userId + 21;
        long bookerId = userId + 30;
        LocalDateTime start1 = start.minusHours(1);
        LocalDateTime end1 = end.plusDays(1);
        LocalDateTime start2 = start.minusDays(1);
        LocalDateTime end2 = end.plusHours(1);
        User owner1 = new User(ownerId1, userName, email);
        User owner2 = new User(ownerId2, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item2 = new Item(itemId + 2, itemName, itemDescription, true, owner2, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto2 = new ItemDto(item2.getId(), itemName, itemDescription, item2.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start1, end1, item1, booker, Status.WAITING);
        Booking booking2 = new Booking(bookingId2, start2, end2, item2, booker, Status.APPROVED);
        List<Booking> usersBookings = List.of(booking1, booking2);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start1, end1, itemDto1, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto2 = new BookingOutputDto(
                bookingId2, start2, end2, itemDto2, bookerDto, Status.APPROVED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto2);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto2 = new BookingOutputDto(
                booking2.getId(), booking2.getStart(), booking2.getEnd(), itemDto2, bookerDto, booking2.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto2);
        when(userRepository.findById(ownerId1)).thenReturn(Optional.of(owner1));
        when(bookingRepository.findAllByItem_Owner_IdAndStartBeforeAndEndAfter(
                eq(ownerId1), any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllOwnersBookings(ownerId1, "current", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(ownerId1);
        inOrder.verify(bookingRepository).findAllByItem_Owner_IdAndStartBeforeAndEndAfter(
                eq(ownerId1), any(LocalDateTime.class), any(LocalDateTime.class), any());
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllOwnersBookings_whenStateIsWaiting_thenReturnedWaitingBookingOnly() {
        long bookingId1 = bookingId + 10;
        long bookingId2 = bookingId + 11;
        long bookingId3 = bookingId + 12;
        Long ownerId1 = userId + 20;
        Long ownerId2 = userId + 21;
        Long ownerId3 = userId + 22;
        long bookerId = userId + 30;
        LocalDateTime start1 = start.minusHours(1);
        LocalDateTime end1 = end.plusDays(1);
        LocalDateTime start2 = start.minusDays(1);
        LocalDateTime end2 = end.plusHours(1);
        LocalDateTime start3 = start.plusMonths(1);
        LocalDateTime end3 = end.plusMonths(2);
        User owner1 = new User(ownerId1, userName, email);
        User owner2 = new User(ownerId2, userName, email);
        User owner3 = new User(ownerId3, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item2 = new Item(itemId + 2, itemName, itemDescription, true, owner2, null);
        Item item3 = new Item(itemId + 3, itemName, itemDescription, true, owner3, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto2 = new ItemDto(item2.getId(), itemName, itemDescription, item2.getAvailable(), null);
        ItemDto itemDto3 = new ItemDto(item3.getId(), itemName, itemDescription, item3.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start1, end1, item1, booker, Status.WAITING);
        Booking booking2 = new Booking(bookingId2, start2, end2, item2, booker, Status.WAITING);
        Booking booking3 = new Booking(bookingId3, start3, end3, item3, booker, Status.WAITING);
        List<Booking> usersBookings = List.of(booking1, booking2, booking3);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start1, end1, itemDto1, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto2 = new BookingOutputDto(
                bookingId2, start2, end2, itemDto2, bookerDto, Status.WAITING);
        BookingOutputDto expectedBookingDto3 = new BookingOutputDto(
                bookingId3, start3, end3, itemDto3, bookerDto, Status.WAITING);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto2, expectedBookingDto3);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto2 = new BookingOutputDto(
                booking2.getId(), booking2.getStart(), booking2.getEnd(), itemDto2, bookerDto, booking2.getStatus());
        BookingOutputDto returnedBookingOutputDto3 = new BookingOutputDto(
                booking3.getId(), booking3.getStart(), booking3.getEnd(), itemDto3, bookerDto, booking3.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto2, returnedBookingOutputDto3);
        when(userRepository.findById(ownerId1)).thenReturn(Optional.of(owner1));
        when(bookingRepository.findAllByItemOwnerIdAndStatus(eq(ownerId1), eq(Status.WAITING), any(PageRequest.class)))
                .thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllOwnersBookings(ownerId1, "waiting", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(ownerId1);
        inOrder.verify(bookingRepository).findAllByItemOwnerIdAndStatus(
                eq(ownerId1), eq(Status.WAITING), any(PageRequest.class));
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }

    @Test
    void getAllOwnersBookings_whenStateIsRejected_thenReturnedRejectedBookingOnly() {
        long bookingId1 = bookingId + 10;
        long bookingId3 = bookingId + 12;
        Long ownerId1 = userId + 20;
        Long ownerId3 = userId + 22;
        long bookerId = userId + 30;
        LocalDateTime start1 = start.minusHours(1);
        LocalDateTime end1 = end.plusDays(1);
        LocalDateTime start3 = start.plusMonths(1);
        LocalDateTime end3 = end.plusMonths(2);
        User owner1 = new User(ownerId1, userName, email);
        User owner3 = new User(ownerId3, userName, email);
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
        Item item1 = new Item(itemId + 1, itemName, itemDescription, true, owner1, null);
        Item item3 = new Item(itemId + 3, itemName, itemDescription, true, owner3, null);
        ItemDto itemDto1 = new ItemDto(item1.getId(), itemName, itemDescription, item1.getAvailable(), null);
        ItemDto itemDto3 = new ItemDto(item3.getId(), itemName, itemDescription, item3.getAvailable(), null);
        Booking booking1 = new Booking(bookingId1, start1, end1, item1, booker, Status.REJECTED);
        Booking booking3 = new Booking(bookingId3, start3, end3, item3, booker, Status.REJECTED);
        List<Booking> usersBookings = List.of(booking1, booking3);
        BookingOutputDto expectedBookingDto1 = new BookingOutputDto(
                bookingId1, start1, end1, itemDto1, bookerDto, Status.REJECTED);
        BookingOutputDto expectedBookingDto3 = new BookingOutputDto(
                bookingId3, start3, end3, itemDto3, bookerDto, Status.REJECTED);
        List<BookingOutputDto> expectedBookingsDto = List.of(
                expectedBookingDto1, expectedBookingDto3);
        BookingOutputDto returnedBookingOutputDto1 = new BookingOutputDto(
                booking1.getId(), booking1.getStart(), booking1.getEnd(), itemDto1, bookerDto, booking1.getStatus());
        BookingOutputDto returnedBookingOutputDto3 = new BookingOutputDto(
                booking3.getId(), booking3.getStart(), booking3.getEnd(), itemDto3, bookerDto, booking3.getStatus());
        List<BookingOutputDto> returnedBookingsOutputDto = List.of(
                returnedBookingOutputDto1, returnedBookingOutputDto3);
        when(userRepository.findById(ownerId1)).thenReturn(Optional.of(owner1));
        when(bookingRepository.findAllByItemOwnerIdAndStatus(eq(ownerId1), eq(Status.REJECTED), any(PageRequest.class)))
                .thenReturn(usersBookings);
        when(bookingMapper.map(usersBookings)).thenReturn(returnedBookingsOutputDto);

        List<BookingOutputDto> resultUserBookingsOutputDto =
                bookingService.getAllOwnersBookings(ownerId1, "rejected", from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository, bookingMapper);
        inOrder.verify(userRepository).findById(ownerId1);
        inOrder.verify(bookingRepository).findAllByItemOwnerIdAndStatus(
                eq(ownerId1), eq(Status.REJECTED), any(PageRequest.class));
        inOrder.verify(bookingMapper).map(usersBookings);
        assertEquals(expectedBookingsDto, resultUserBookingsOutputDto);
    }


    @Test
    void getAllOwnersBookings_whenOwnerNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> bookingService.getAllOwnersBookings(userId, "all", from, size));

        verify(userRepository).findById(userId);
    }
}
