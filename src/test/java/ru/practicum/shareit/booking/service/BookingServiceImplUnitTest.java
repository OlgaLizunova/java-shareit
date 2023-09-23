package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplUnitTest {
    private final BookingService bookingService;
    @MockBean
    private final ItemRepository itemRepository;
    @MockBean
    private final UserRepository userRepository;
    @MockBean
    private final BookingRepository bookingRepository;

    private Item item1;
    private Item item2;
    private User booker1;
    private User booker2;
    private User owner1;
    private User owner2;
    private Booking booking1;
    private Booking booking2;
    private Comment comment;
    private ItemRequest request;
    private Status status = Status.APPROVED;

    @BeforeEach
    void setUp() {
        booker1 = User.builder()
                .id(1L)
                .name("Маша")
                .email("masha@mail.ru")
                .build();
        booker2 = User.builder()
                .name("Саша")
                .email("sasha@mail.ru")
                .build();

        owner1 = User.builder()
                .name("Дима")
                .email("dima@mail.ru")
                .build();

        owner2 = User.builder()
                .name("Вася")
                .email("vasya@mail.ru")
                .build();

        User requester = User.builder()
                .name("Ольга")
                .email("olga@mail.ru")
                .build();

        item1 = new Item(0L, "item1", "description1", true, owner1, null);

        item2 = new Item(0L, "item2", "description2", false, owner2, null);

        comment = new Comment(0L, "comment1", now(), item1, requester);

        booking1 = new Booking(0L, null, null, item1, booker1, status);
        booking2 = new Booking(0L, null, null, item1, booker2, status);

        request = new ItemRequest(0L, "description1", now().minusMinutes(1), requester);
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenAddBookingWhenUserNotFound() {
        BookingInputDto bookingInputDto = new BookingInputDto(now().plusDays(1),
                now().plusDays(10), item1.getId());
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.addBooking(bookingInputDto, 3L));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenAddBookingWhenItemNotFound() {
        BookingInputDto bookingInputDto = new BookingInputDto(now().plusDays(1),
                now().plusDays(10), item2.getId());
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booker1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.addBooking(bookingInputDto, 1L));
    }
}
