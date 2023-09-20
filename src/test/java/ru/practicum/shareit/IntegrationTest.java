package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.controller.State;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.comment.dto.CommentInputDto;
import ru.practicum.shareit.item.comment.dto.CommentOutputDto;
import ru.practicum.shareit.item.comment.dto.SavedCommentOutputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsOutputDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"spring.datasource.driver-class-name=org.h2.Driver"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {
    @Autowired
    private final UserServiceImpl userService;
    @Autowired
    private final ItemServiceImpl itemService;
    @Autowired
    private final BookingServiceImpl bookingService;
    @Autowired
    private final ItemRequestServiceImpl requestService;
    UserDto userInputDto1 = UserDto.builder()
            .name("Walter")
            .email("white@mail.ru")
            .build();
    UserDto userInputDto3 = UserDto.builder()
            .name("Jesse")
            .email("pinkman@mail.ru")
            .build();
    UserDto userInputDto4 = UserDto.builder()
            .name("Vince")
            .email("gilligan@mail.ru")
            .build();
    UserDto userInputDto5 = UserDto.builder()
            .name("Gustavo")
            .email("Fring@mail.ru")
            .build();
    UserDto userInputDto6 = UserDto.builder()
            .name("heisenberg")
            .email("Breaking@Bad.us")
            .build();
    ItemDto inputItemDto1 = ItemDto.builder()
            .name("Большой набор юного химика")
            .description("Поможет научиться много чего синтезировать")
            .available(true)
            .build();
    ItemDto inputItemDto2 = ItemDto.builder()
            .name("Большая пластиковая ёмкость")
            .description("Не взаимодействует с плавиковой кислотой")
            .available(true)
            .build();
    ItemDto inputItemDto3 = ItemDto.builder()
            .name("Набор видеокассет с сериалом")
            .description("Отличный сериал. Вам обязательно понравится.")
            .available(true)
            .build();
    ItemRequestInputDto itemRequestInputDto1 = ItemRequestInputDto.builder()
            .description("Нужна ванна из пластика. Или бочка.")
            .build();
    ItemRequestInputDto itemRequestInputDto2 = ItemRequestInputDto.builder()
            .description("Пригодился бы респиратор из кабинета химии и прочие приспособления")
            .build();
    LocalDateTime bookingStart1 = now().plusHours(1);
    LocalDateTime bookingEnd1 = now().plusDays(1);
    LocalDateTime bookingStart2 = now().minusMonths(2);
    LocalDateTime bookingEnd2 = now().minusDays(10);
    LocalDateTime bookingStart3 = now().minusDays(3);
    LocalDateTime bookingEnd3 = now().plusHours(3);
    LocalDateTime bookingStart4 = now().minusHours(4);
    LocalDateTime bookingEnd4 = now().plusHours(4);
    LocalDateTime bookingStart5 = now().minusDays(5);
    LocalDateTime bookingEnd5 = now().minusHours(5);
    LocalDateTime bookingStart6 = now().plusHours(6);
    LocalDateTime bookingEnd6 = now().plusDays(6);

    BookingInputDto bookingInputDto1 = BookingInputDto.builder()
            .itemId(1L)
            .start(bookingStart1)
            .end(bookingEnd1)
            .build();
    BookingInputDto bookingInputDto2 = BookingInputDto.builder()
            .itemId(2L)
            .start(bookingStart2)
            .end(bookingEnd2)
            .build();
    BookingInputDto bookingInputDto3 = BookingInputDto.builder()
            .itemId(3L)
            .start(bookingStart3)
            .end(bookingEnd3)
            .build();
    BookingInputDto bookingInputDto4 = BookingInputDto.builder()
            .itemId(1L)
            .start(bookingStart4)
            .end(bookingEnd4)
            .build();
    BookingInputDto bookingInputDto5 = BookingInputDto.builder()
            .itemId(2L)
            .start(bookingStart5)
            .end(bookingEnd5)
            .build();
    BookingInputDto bookingInputDto6 = BookingInputDto.builder()
            .itemId(3L)
            .start(bookingStart6)
            .end(bookingEnd6)
            .build();

    static long bookingId2;
    static long bookingId3;
    static long bookingId4;
    static long bookingId5;
    static long bookingId6;
    static long bookingId7;
    static long bookerId5;
    static long bookerId6;

    static BookingOutputDto updatedBookingDto;

    @Test
    @Order(1)
    void context() {
        assertAll(
                () -> assertNotNull(userService),
                () -> assertNotNull(itemService),
                () -> assertNotNull(bookingService),
                () -> assertNotNull(requestService));
    }

    @Test
    @Order(2)
    void testUserAddUpdateGetByIdGetAllDeleteById() {
        UserDto secondUserInputDto = UserDto.builder()
                .name("Saul")
                .email("goodman@mail.ru")
                .build();

        UserDto addedUser = userService.addUser(userInputDto1);

        assertAll(
                () -> assertNotNull(addedUser),
                () -> assertEquals(1L, addedUser.getId()),
                () -> assertEquals(userInputDto1.getName(), addedUser.getName()),
                () -> assertEquals(userInputDto1.getName(), addedUser.getName()),
                () -> assertEquals(userInputDto1.getEmail(), addedUser.getEmail()),
                () -> assertNotSame(userInputDto1, addedUser)
        );

        UserDto newUserNameDto = UserDto.builder()
                .name("Hank")
                .build();

        UserDto updatedUserNameDto = userService.updateUser(addedUser.getId(), newUserNameDto);
        assertAll(
                () -> assertNotNull(updatedUserNameDto),
                () -> assertEquals(1L, updatedUserNameDto.getId()),
                () -> assertEquals(newUserNameDto.getName(), updatedUserNameDto.getName()),
                () -> assertEquals(addedUser.getEmail(), updatedUserNameDto.getEmail()),
                () -> assertNotSame(newUserNameDto, updatedUserNameDto),
                () -> assertNotSame(addedUser, updatedUserNameDto)
        );

        UserDto newUserMailDto = UserDto.builder()
                .email("schrader@mail.ru")
                .build();

        UserDto updatedUserMailDto = userService.updateUser(addedUser.getId(), newUserMailDto);

        assertAll(
                () -> assertNotNull(updatedUserMailDto),
                () -> assertEquals(1L, updatedUserMailDto.getId()),
                () -> assertEquals(updatedUserNameDto.getName(), updatedUserMailDto.getName()),
                () -> assertEquals(newUserMailDto.getEmail(), updatedUserMailDto.getEmail()),
                () -> assertNotSame(newUserMailDto, updatedUserMailDto),
                () -> assertNotSame(addedUser, updatedUserMailDto)
        );

        UserDto newUserDataDto = UserDto.builder()
                .name("Walter")
                .email("white@mail.ru")
                .build();

        UserDto updatedUserDataDTO = userService.updateUser(addedUser.getId(), newUserDataDto);

        assertAll(
                () -> assertNotNull(updatedUserDataDTO),
                () -> assertEquals(1L, updatedUserDataDTO.getId()),
                () -> assertEquals(newUserDataDto.getName(), updatedUserDataDTO.getName()),
                () -> assertEquals(newUserDataDto.getEmail(), updatedUserDataDTO.getEmail()),
                () -> assertNotSame(newUserDataDto, updatedUserDataDTO),
                () -> assertNotSame(addedUser, updatedUserDataDTO)
        );

        UserDto addedSecondUser = userService.addUser(secondUserInputDto);
        List<UserDto> allUsers = userService.getAllUsers();

        assertAll(
                () -> assertNotNull(addedSecondUser),
                () -> assertNotNull(allUsers),
                () -> assertEquals(2L, addedSecondUser.getId()),
                () -> assertEquals(secondUserInputDto.getName(), addedSecondUser.getName()),
                () -> assertEquals(secondUserInputDto.getName(), addedSecondUser.getName()),
                () -> assertEquals(secondUserInputDto.getEmail(), addedSecondUser.getEmail()),
                () -> assertNotSame(secondUserInputDto, addedSecondUser),
                () -> assertEquals(2, allUsers.size()),
                () -> assertThat(allUsers, hasItem(updatedUserDataDTO)),
                () -> assertThat(allUsers, hasItem(addedSecondUser))
        );

        userService.deleteUserById(1L);
        List<UserDto> allUsersAfterDelete = userService.getAllUsers();

        assertAll(
                () -> assertNotNull(allUsersAfterDelete),
                () -> assertEquals(2L, allUsersAfterDelete.get(0).getId()),
                () -> assertEquals(1, allUsersAfterDelete.size()),
                () -> assertThat(allUsersAfterDelete, not(hasItem(updatedUserDataDTO))),
                () -> assertThat(allUsersAfterDelete, hasItem(addedSecondUser))
        );
    }

    @Test
    @Order(3)
    void testItemWithoutRequestAddUpdate() {
        ItemDto addedItemInputDto = itemService.addItem(inputItemDto1, 2L);

        assertAll(
                () -> assertNotNull(addedItemInputDto),
                () -> assertEquals(1L, addedItemInputDto.getId()),
                () -> assertEquals(inputItemDto1.getName(), addedItemInputDto.getName()),
                () -> assertEquals(inputItemDto1.getDescription(), addedItemInputDto.getDescription()),
                () -> assertEquals(inputItemDto1.getAvailable(), addedItemInputDto.getAvailable()),
                () -> assertNull(addedItemInputDto.getRequestId()),
                () -> assertNotSame(inputItemDto1, addedItemInputDto)
        );

        inputItemDto1.setName("Малый набор юного химика");
        inputItemDto1.setDescription("Если хочешь научиться понемногу химичить.");

        ItemDto updatedItemDto = itemService.updateItem(1L, inputItemDto1, 2L);

        assertAll(
                () -> assertNotNull(updatedItemDto),
                () -> assertEquals(1L, updatedItemDto.getId()),
                () -> assertEquals(inputItemDto1.getName(), updatedItemDto.getName()),
                () -> assertEquals(inputItemDto1.getDescription(), updatedItemDto.getDescription()),
                () -> assertEquals(inputItemDto1.getAvailable(), updatedItemDto.getAvailable()),
                () -> assertNull(updatedItemDto.getRequestId()),
                () -> assertNotSame(inputItemDto1, updatedItemDto)
        );
    }

    @Test
    @Order(4)
    void testRequestWithoutItemAddGetByIdGetByRequester() {
        long requesterId = 3L;
        userService.addUser(userInputDto3);

        ItemRequestOutDto addedRequestOutDto1 = requestService.addItemRequest(itemRequestInputDto1, requesterId);

        assertAll(
                () -> assertNotNull(addedRequestOutDto1),
                () -> assertEquals(1L, addedRequestOutDto1.getId()),
                () -> assertEquals(itemRequestInputDto1.getDescription(), addedRequestOutDto1.getDescription()),
                () -> assertNotNull(addedRequestOutDto1.getItems()),
                () -> assertTrue(ChronoUnit.MILLIS.between(LocalDateTime.now(),
                        addedRequestOutDto1.getCreated()) < 200)
        );

        ItemRequestOutDto returnedRequestOutDto2 = requestService.getItemRequestById(1L, requesterId);

        assertAll(
                () -> assertNotNull(returnedRequestOutDto2),
                () -> assertEquals(1L, returnedRequestOutDto2.getId()),
                () -> assertEquals(itemRequestInputDto1.getDescription(), returnedRequestOutDto2.getDescription()),
                () -> assertNotNull(returnedRequestOutDto2.getItems()),
                () -> assertEquals(0, returnedRequestOutDto2.getItems().size()),
                () -> assertTrue(ChronoUnit.MILLIS.between(LocalDateTime.now(),
                        returnedRequestOutDto2.getCreated()) < 200)
        );

        ItemRequestOutDto addedRequestOutDto2 =
                requestService.addItemRequest(itemRequestInputDto2, 3L);
        List<ItemRequestOutDto> allItemRequestsDto =
                requestService.getAllRequestersItemRequests(requesterId, 0, 25);

        assertAll(
                () -> assertNotNull(allItemRequestsDto),
                () -> assertEquals(2, allItemRequestsDto.size()),
                () -> assertEquals(addedRequestOutDto2.getId(), allItemRequestsDto.get(0).getId()),
                () -> assertEquals(addedRequestOutDto2.getDescription(), allItemRequestsDto.get(0).getDescription()),
                () -> assertEquals(addedRequestOutDto2.getCreated(), allItemRequestsDto.get(0).getCreated()),
                () -> assertNotNull(allItemRequestsDto.get(0).getItems()),
                () -> assertEquals(0, allItemRequestsDto.get(0).getItems().size()),
                () -> assertTrue(allItemRequestsDto.get(0).getCreated()
                        .isAfter(allItemRequestsDto.get(1).getCreated())),
                () -> assertThat(allItemRequestsDto, hasItem(addedRequestOutDto1)),
                () -> assertThat(allItemRequestsDto, hasItem(addedRequestOutDto2))
        );
    }

    @Test
    @Order(5)
    void testItemWithRequestAddUpdate_getAllRequestersRequests() {
        Long requestId = 1L;
        long itemId = 2L;
        long ownerId = 3L;

        inputItemDto2.setRequestId(requestId);
        ItemDto savedItem = itemService.addItem(inputItemDto2, ownerId);

        assertAll(
                () -> assertNotNull(savedItem),
                () -> assertEquals(2L, savedItem.getId()),
                () -> assertEquals(inputItemDto2.getName(), savedItem.getName()),
                () -> assertEquals(inputItemDto2.getDescription(), savedItem.getDescription()),
                () -> assertEquals(inputItemDto2.getAvailable(), savedItem.getAvailable()),
                () -> assertEquals(requestId, savedItem.getRequestId())
        );

        inputItemDto2.setAvailable(true);

        ItemDto updatedItem = itemService.updateItem(itemId, inputItemDto2, ownerId);

        assertAll(
                () -> assertNotNull(updatedItem),
                () -> assertEquals(itemId, updatedItem.getId()),
                () -> assertEquals(inputItemDto2.getName(), updatedItem.getName()),
                () -> assertEquals(inputItemDto2.getDescription(), updatedItem.getDescription()),
                () -> assertEquals(inputItemDto2.getAvailable(), updatedItem.getAvailable()),
                () -> assertEquals(1L, updatedItem.getRequestId()),
                () -> assertNotSame(inputItemDto2, updatedItem)
        );

        List<ItemRequestOutDto> outRequests =
                requestService.getAllRequestersItemRequests(3L, 0, 25);

        assertAll(
                () -> assertNotNull(outRequests),
                () -> assertEquals(2, outRequests.size()),
                () -> assertNotNull(outRequests.get(0).getItems()),
                () -> assertEquals(0, outRequests.get(0).getItems().size()),
                () -> assertNotNull(outRequests.get(1).getItems()),
                () -> assertEquals(1, outRequests.get(1).getItems().size()),
                () -> assertThat(outRequests.get(1).getItems(), hasItem(updatedItem))
        );
    }

    @Test
    @Order(6)
    void testItemRequestGetByIdGetAll() {
        long userId = 3L;
        long itemId = 2L;
        long requestId = 1L;
        ItemRequestOutDto returnedItemRequestOutDto = requestService.getItemRequestById(requestId, userId);

        assertNotNull(returnedItemRequestOutDto);
        assertEquals(requestId, returnedItemRequestOutDto.getId());
        assertEquals(itemRequestInputDto1.getDescription(), returnedItemRequestOutDto.getDescription());
        assertTrue(ChronoUnit.MILLIS.between(LocalDateTime.now(),
                returnedItemRequestOutDto.getCreated()) < 200);

        assertNotNull(returnedItemRequestOutDto.getItems());
        assertEquals(1, returnedItemRequestOutDto.getItems().size());

        assertTrue(returnedItemRequestOutDto.getItems().stream()
                .anyMatch(itemDto -> itemDto.getId() == (itemId)));
        assertTrue(returnedItemRequestOutDto.getCreated().isBefore(LocalDateTime.now()));

        List<ItemRequestOutDto> allItemRequestsDto = requestService.getAllItemRequests(2L, 0, 25);

        ItemRequestOutDto itemRequestOutDto0 = allItemRequestsDto.get(0);
        ItemRequestOutDto itemRequestOutDto1 = allItemRequestsDto.get(1);

        assertAll(
                () -> assertNotNull(allItemRequestsDto),
                () -> assertEquals(2, allItemRequestsDto.size()),
                () -> assertEquals(2L, itemRequestOutDto0.getId()),
                () -> assertEquals(itemRequestInputDto2.getDescription(), itemRequestOutDto0.getDescription()),
                () -> assertTrue(ChronoUnit.MILLIS.between(LocalDateTime.now(),
                        itemRequestOutDto0.getCreated()) < 200),
                () -> assertNotNull(itemRequestOutDto0.getItems()),
                () -> assertEquals(0, itemRequestOutDto0.getItems().size()),

                () -> assertEquals(1L, itemRequestOutDto1.getId()),
                () -> assertEquals(itemRequestInputDto1.getDescription(), itemRequestOutDto1.getDescription()),
                () -> assertTrue(ChronoUnit.MILLIS.between(LocalDateTime.now(),
                        itemRequestOutDto1.getCreated()) < 200),
                () -> assertNotNull(itemRequestOutDto1.getItems()),
                () -> assertEquals(1, itemRequestOutDto1.getItems().size()),
                () -> assertTrue(itemRequestOutDto1.getItems().stream()
                        .anyMatch(itemDto -> itemDto.getId() == (2L)))
        );
    }

    @Test
    @Order(7)
    void testBookingAddUpdate() {
        long itemId = 2L;
        long bookerId = 2L;
        long ownerId = 3L;
        long bookingId = 1L;

        LocalDateTime bookingStart = LocalDateTime.now().plusDays(1);
        LocalDateTime bookingEnd = LocalDateTime.now().plusDays(15);

        BookingInputDto bookingInputDto = BookingInputDto.builder()
                .itemId(itemId)
                .start(bookingStart)
                .end(bookingEnd)
                .build();

        BookingOutputDto addedBookingDto = bookingService.addBooking(bookingInputDto, bookerId);

        assertAll(
                () -> assertNotNull(addedBookingDto),
                () -> assertEquals(1L, addedBookingDto.getId()),
                () -> assertEquals(Status.WAITING, addedBookingDto.getStatus()),
                () -> assertEquals(bookingInputDto.getStart(), addedBookingDto.getStart()),
                () -> assertEquals(bookingInputDto.getEnd(), addedBookingDto.getEnd()),
                () -> assertEquals(bookerId, addedBookingDto.getBooker().getId()),
                () -> assertEquals(itemId, addedBookingDto.getItem().getId())
        );

        updatedBookingDto = bookingService.updateBooking(bookingId, ownerId, true);

        assertAll(
                () -> assertNotNull(updatedBookingDto),
                () -> assertEquals(bookingId, updatedBookingDto.getId()),
                () -> assertEquals(Status.APPROVED, updatedBookingDto.getStatus()),
                () -> assertEquals(bookerId, updatedBookingDto.getBooker().getId()),
                () -> assertEquals(itemId, updatedBookingDto.getItem().getId()),
                () -> assertEquals(addedBookingDto.getItem().getName(), updatedBookingDto.getItem().getName())
        );
    }

    @Test
    @Order(8)
    void testBookingGetAllUsersBookingsWithStateAllRejectWaiting() {
        long ownerId2 = 2L;
        long ownerId3 = 3L;

        UserDto owner3 = userService.addUser(userInputDto4);
        long ownerId4 = owner3.getId();

        UserDto bookerDto = userService.addUser(userInputDto5);
        bookerId5 = bookerDto.getId();

        itemService.addItem(inputItemDto3, ownerId4);

        BookingOutputDto bookingOutputDto1 = bookingService.addBooking(bookingInputDto1, bookerId5);
        BookingOutputDto bookingOutputDto2 = bookingService.addBooking(bookingInputDto2, bookerId5);
        BookingOutputDto bookingOutputDto3 = bookingService.addBooking(bookingInputDto3, bookerId5);
        bookingId2 = bookingOutputDto1.getId();
        bookingId3 = bookingOutputDto2.getId();
        bookingId4 = bookingOutputDto3.getId();

        BookingOutputDto bookingOutputDtoWait1 = bookingService.getBookingByIdAndBookerId(bookingId2, bookerId5);
        BookingOutputDto bookingOutputDtoApprove2 = bookingService.updateBooking(bookingId3, ownerId3, true);
        BookingOutputDto bookingOutputDtoReject3 = bookingService.updateBooking(bookingId4, ownerId4, false);

        assertAll(
                () -> assertEquals(Status.WAITING, bookingOutputDtoWait1.getStatus()),
                () -> assertEquals(Status.APPROVED, bookingOutputDtoApprove2.getStatus()),
                () -> assertEquals(Status.REJECTED, bookingOutputDtoReject3.getStatus())
        );

        List<BookingOutputDto> bookingsOutDto =
                bookingService.getAllUsersBookings(bookerId5, State.ALL, 0, 25);

        assertAll(
                () -> assertNotNull(bookingsOutDto),
                () -> assertEquals(3, bookingsOutDto.size()),
                () -> assertThat(bookingsOutDto, hasItem(bookingOutputDtoWait1)),
                () -> assertThat(bookingsOutDto, hasItem(bookingOutputDtoApprove2)),
                () -> assertThat(bookingsOutDto, hasItem(bookingOutputDtoReject3))
        );

        List<BookingOutputDto> bookingsOutDtoWait =
                bookingService.getAllUsersBookings(bookerId5, State.WAITING, 0, 25);
        assertAll(
                () -> assertNotNull(bookingsOutDtoWait),
                () -> assertEquals(1, bookingsOutDtoWait.size()),
                () -> assertThat(bookingsOutDtoWait, hasItem(bookingOutputDtoWait1))
        );

        BookingOutputDto bookingOutputDtoReject1 = bookingService.updateBooking(bookingId2, ownerId2, false);

        List<BookingOutputDto> bookingsOutDtoRejected =
                bookingService.getAllUsersBookings(bookerId5, State.REJECTED, 0, 25);

        assertAll(
                () -> assertNotNull(bookingsOutDtoRejected),
                () -> assertEquals(2, bookingsOutDtoRejected.size()),
                () -> assertThat(bookingsOutDtoRejected, hasItem(bookingOutputDtoReject1)),
                () -> assertThat(bookingsOutDtoRejected, hasItem(bookingOutputDtoReject3))
        );
    }

    @Test
    @Order(9)
    void testBookingGetAllUsersBookingsWithStatePastCurrentFuture() {
        long ownerId2 = 2L;
        long ownerId3 = 3L;
        long ownerId4 = 4L;

        UserDto bookerDto = userService.addUser(userInputDto6);
        bookerId6 = bookerDto.getId();

        BookingOutputDto bookingOutputDto4 = bookingService.addBooking(bookingInputDto4, bookerId6);
        BookingOutputDto bookingOutputDto5 = bookingService.addBooking(bookingInputDto5, bookerId6);
        BookingOutputDto bookingOutputDto6 = bookingService.addBooking(bookingInputDto6, bookerId6);
        bookingId5 = bookingOutputDto4.getId();
        bookingId6 = bookingOutputDto5.getId();
        bookingId7 = bookingOutputDto6.getId();

        BookingOutputDto bookingOutputDtoCurrent = bookingService.updateBooking(bookingId5, ownerId2, true);
        BookingOutputDto bookingOutputDtoPast = bookingService.updateBooking(bookingId6, ownerId3, true);
        BookingOutputDto bookingOutputDtoFuture = bookingService.updateBooking(bookingId7, ownerId4, true);

        List<BookingOutputDto> bookingsOutDtoCurrent =
                bookingService.getAllUsersBookings(bookerId6, State.CURRENT, 0, 25);

        assertAll(
                () -> assertNotNull(bookingsOutDtoCurrent),
                () -> assertEquals(1, bookingsOutDtoCurrent.size()),
                () -> assertThat(bookingsOutDtoCurrent, hasItem(bookingOutputDtoCurrent))
        );

        List<BookingOutputDto> bookingsOutDtoPast =
                bookingService.getAllUsersBookings(bookerId6, State.PAST, 0, 25);

        assertAll(
                () -> assertNotNull(bookingsOutDtoPast),
                () -> assertEquals(1, bookingsOutDtoPast.size()),
                () -> assertThat(bookingsOutDtoPast, hasItem(bookingOutputDtoPast))
        );

        List<BookingOutputDto> bookingsOutDtoFuture =
                bookingService.getAllUsersBookings(bookerId6, State.FUTURE, 0, 25);

        assertAll(
                () -> assertNotNull(bookingsOutDtoFuture),
                () -> assertEquals(1, bookingsOutDtoFuture.size()),
                () -> assertThat(bookingsOutDtoFuture, hasItem(bookingOutputDtoFuture))
        );

    }

    @Test
    @Order(10)
    void testBookingGetAllOwnersBookingsWithStateAllRejectWaitingPastCurrentFuture() {
        long ownerId2 = 2L;
        long ownerId3 = 3L;
        long ownerId4 = 4L;

        UserDto booker7 = userService.addUser(userInputDto1);
        long bookerId7 = booker7.getId();

        BookingOutputDto bookingByIdAndBookerId1 = bookingService.getBookingByIdAndBookerId(bookingId2, bookerId5);
        BookingOutputDto bookingByIdAndBookerId2 = bookingService.getBookingByIdAndBookerId(bookingId3, bookerId5);
        BookingOutputDto bookingByIdAndBookerId3 = bookingService.getBookingByIdAndBookerId(bookingId4, bookerId5);
        BookingOutputDto bookingByIdAndBookerId4 = bookingService.getBookingByIdAndBookerId(bookingId5, bookerId6);
        BookingOutputDto bookingByIdAndBookerId5 = bookingService.getBookingByIdAndBookerId(bookingId6, bookerId6);
        BookingOutputDto bookingByIdAndBookerId6 = bookingService.getBookingByIdAndBookerId(bookingId7, bookerId6);
        bookingInputDto2.setStart(now().plusMonths(5).truncatedTo(ChronoUnit.MILLIS));
        bookingInputDto2.setEnd(now().plusMonths(10).truncatedTo(ChronoUnit.MILLIS));
        bookingInputDto5.setStart(now().plusMonths(15).truncatedTo(ChronoUnit.MILLIS));
        bookingInputDto5.setEnd(now().plusMonths(20).truncatedTo(ChronoUnit.MILLIS));
        BookingOutputDto bookingOutputDto8 = bookingService.addBooking(bookingInputDto2, bookerId7);
        BookingOutputDto bookingOutputDto9 = bookingService.addBooking(bookingInputDto5, bookerId7);

        List<BookingOutputDto> allOwners4Bookings
                = bookingService.getAllOwnersBookings(ownerId4, State.ALL, 0, 25);

        assertAll(
                () -> assertNotNull(allOwners4Bookings),
                () -> assertEquals(2, allOwners4Bookings.size()),
                () -> assertThat(allOwners4Bookings, hasItem(bookingByIdAndBookerId3)),
                () -> assertThat(allOwners4Bookings, hasItem(bookingByIdAndBookerId6))
        );

        List<BookingOutputDto> rejectedOwners2Bookings
                = bookingService.getAllOwnersBookings(ownerId2, State.REJECTED, 0, 25);

        assertAll(
                () -> assertNotNull(rejectedOwners2Bookings),
                () -> assertEquals(1, rejectedOwners2Bookings.size()),
                () -> assertThat(rejectedOwners2Bookings, hasItem(bookingByIdAndBookerId1))
        );

        List<BookingOutputDto> waitingOwners3Bookings
                = bookingService.getAllOwnersBookings(ownerId3, State.WAITING, 0, 25);

        assertAll(
                () -> assertNotNull(waitingOwners3Bookings),
                () -> assertEquals(2, waitingOwners3Bookings.size()),
                () -> assertThat(waitingOwners3Bookings, hasItem(bookingOutputDto8)),
                () -> assertThat(waitingOwners3Bookings, hasItem(bookingOutputDto9))
        );

        List<BookingOutputDto> futureOwners3Bookings
                = bookingService.getAllOwnersBookings(ownerId3, State.FUTURE, 0, 25);

        assertAll(
                () -> assertNotNull(futureOwners3Bookings),
                () -> assertEquals(3, futureOwners3Bookings.size()),
                () -> assertThat(futureOwners3Bookings, hasItem(bookingOutputDto8)),
                () -> assertThat(futureOwners3Bookings, hasItem(bookingOutputDto9)),
                () -> assertThat(futureOwners3Bookings, hasItem(updatedBookingDto))
        );

        List<BookingOutputDto> currentOwners2Bookings
                = bookingService.getAllOwnersBookings(ownerId2, State.CURRENT, 0, 25);

        assertAll(
                () -> assertNotNull(currentOwners2Bookings),
                () -> assertEquals(1, currentOwners2Bookings.size()),
                () -> assertThat(currentOwners2Bookings, hasItem(bookingByIdAndBookerId4))
        );

        List<BookingOutputDto> pastOwners3Bookings
                = bookingService.getAllOwnersBookings(ownerId3, State.PAST, 0, 25);

        assertAll(
                () -> assertNotNull(pastOwners3Bookings),
                () -> assertEquals(2, pastOwners3Bookings.size()),
                () -> assertThat(pastOwners3Bookings, hasItem(bookingByIdAndBookerId2)),
                () -> assertThat(pastOwners3Bookings, hasItem(bookingByIdAndBookerId5))
        );
    }


    @Test
    @Order(11)
    void addComment() {
        CommentInputDto commentInputDto = CommentInputDto.builder()
                .text("Вот это то, что надо. Прекрасная ванна, не то, что домашняя!")
                .build();

        SavedCommentOutputDto savedCommentOutputDto = itemService.addComment(commentInputDto, 2L, 6L);

        assertAll(
                () -> assertNotNull(savedCommentOutputDto),
                () -> assertEquals(1L, savedCommentOutputDto.getId()),
                () -> assertEquals(commentInputDto.getText(), savedCommentOutputDto.getText()),
                () -> assertEquals("heisenberg", savedCommentOutputDto.getAuthorName()),
                () -> assertTrue(savedCommentOutputDto.getCreated().isBefore(LocalDateTime.now()))
        );
    }


    @Test
    @Order(12)
    void getOwnersItemById() {
        long ownerId1 = 3L;
        long itemId = 2L;

        ItemWithCommentsOutputDto itemOutDto = itemService.getItemById(itemId, ownerId1);

        assertAll(
                () -> assertNotNull(itemOutDto),
                () -> assertEquals(itemId, itemOutDto.getId())
        );

        List<CommentOutputDto> comments = itemOutDto.getComments();

        assertAll(
                () -> assertNotNull(comments),
                () -> assertEquals(1, comments.size()),
                () -> assertEquals(1L, comments.get(0).getId()),
                () -> assertNotNull(itemOutDto.getNextBooking()),
                () -> assertEquals(1L, itemOutDto.getNextBooking().getId()),
                () -> assertNotNull(itemOutDto.getLastBooking()),
                () -> assertEquals(6L, itemOutDto.getLastBooking().getId())
        );
    }

    @Test
    @Order(13)
    void getItemByIdWhenNotOwner() {
        long itemId = 2L;
        long userId = 4L;
        ItemWithCommentsOutputDto itemOutDto1 = itemService.getItemById(itemId, userId);

        assertAll(
                () -> assertNotNull(itemOutDto1),
                () -> assertEquals(itemId, itemOutDto1.getId())
        );

        List<CommentOutputDto> comments1 = itemOutDto1.getComments();

        assertAll(
                () -> assertNotNull(comments1),
                () -> assertEquals(1, comments1.size()),
                () -> assertEquals(1L, comments1.get(0).getId()),
                () -> assertNull(itemOutDto1.getNextBooking()),
                () -> assertNull(itemOutDto1.getLastBooking())
        );

        ItemWithCommentsOutputDto itemOutDto2 = itemService.getItemById(itemId, userId);

        assertAll(
                () -> assertNotNull(itemOutDto2),
                () -> assertEquals(itemId, itemOutDto2.getId())
        );

        List<CommentOutputDto> comments2 = itemOutDto2.getComments();

        assertAll(
                () -> assertNotNull(comments2),
                () -> assertEquals(1, comments2.size()),
                () -> assertFalse(comments2.get(0).getText().isBlank()),
                () -> assertNull(itemOutDto2.getNextBooking()),
                () -> assertNull(itemOutDto2.getLastBooking())
        );
    }

    @Test
    @Order(14)
    void findItems() {

        itemService.getItemById(1L, 2L);

        List<ItemDto> foundItems = itemService.findItems("набор", 0, 25);

        assertEquals(2, foundItems.size());

        ItemDto itemDto1 = foundItems.get(0);
        ItemDto itemDto2 = foundItems.get(1);

        assertAll(
                () -> assertEquals(2, foundItems.size()),
                () -> assertEquals(1L, itemDto1.getId()),
                () -> assertTrue(itemDto1.getAvailable()),
                () -> assertEquals("Малый набор юного химика", itemDto1.getName()),
                () -> assertEquals(3L, itemDto2.getId()),
                () -> assertTrue(itemDto2.getAvailable()),
                () -> assertEquals("Набор видеокассет с сериалом", itemDto2.getName())
        );

    }

}
