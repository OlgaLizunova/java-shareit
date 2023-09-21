package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    ItemRequestRepository requestRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    BookingRepository bookingRepository;

    Status status = Status.APPROVED;
    User booker1;
    User booker2;
    User owner1;
    User owner2;

    Item item1;
    Item item2;
    Item item3;
    Item item4;

    Booking booking1;
    Booking booking2;
    Booking booking3;
    Booking booking4;

    PageRequest pageable;

    @BeforeEach
    void setUp() {

        booker1 = User.builder()
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

        booker1 = userRepository.save(booker1);
        booker2 = userRepository.save(booker2);
        owner1 = userRepository.save(owner1);
        owner2 = userRepository.save(owner2);

        item1 = new Item(0L, "item1", "description1", true, owner1, null);
        item2 = new Item(0L, "item2", "description2", true, owner2, null);
        item3 = new Item(0L, "item3", "description3", true, owner2, null);
        item4 = new Item(0L, "item4", "description4", true, owner1, null);

        item1 = itemRepository.save(item1);
        item2 = itemRepository.save(item2);
        item3 = itemRepository.save(item3);
        item4 = itemRepository.save(item4);

        booking1 = new Booking(0L, null, null, item1, booker1, status);
        booking2 = new Booking(0L, null, null, item1, booker2, status);
        booking3 = new Booking(0L, null, null, item2, booker1, status);
        booking4 = new Booking(0L, null, null, item2, booker2, status);

        pageable = PageRequest.of(0, 25, Sort.Direction.DESC, "id");
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        requestRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void findLastBookingsForItems() {
        LocalDateTime start1 = now().minusDays(10);
        LocalDateTime start2 = now().minusDays(20);
        LocalDateTime end1 = now().plusDays(10);
        LocalDateTime end2 = now().minusDays(10);

        LocalDateTime start3 = now().minusDays(5);
        LocalDateTime start4 = now().plusDays(15);
        LocalDateTime end3 = now().plusDays(15);
        LocalDateTime end4 = now().plusDays(5);

        booking1.setStart(start1);
        booking2.setStart(start2);
        booking3.setStart(start3);
        booking4.setStart(start4);
        booking1.setEnd(end1);
        booking2.setEnd(end2);
        booking3.setEnd(end3);
        booking4.setEnd(end4);

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
        booking4 = bookingRepository.save(booking4);

        List<Booking> lastBookingsForItems = bookingRepository.findLastBookingsForItems(
                List.of(item1.getId(), item2.getId()), status.toString(), now());


        assertAll(
                () -> assertNotNull(lastBookingsForItems),
                () -> assertEquals(2, lastBookingsForItems.size()),
                () -> assertThat(lastBookingsForItems, hasItem(booking1)),
                () -> assertThat(lastBookingsForItems, hasItem(booking3))
        );
    }

    @Test
    void findNextBookingsForItems() {
        LocalDateTime start1 = now().minusDays(10);
        LocalDateTime start2 = now().plusDays(20);
        LocalDateTime end1 = now().plusDays(10);
        LocalDateTime end2 = now().plusDays(30);

        LocalDateTime start3 = now().minusDays(5);
        LocalDateTime start4 = now().plusDays(15);
        LocalDateTime end3 = now().plusDays(15);
        LocalDateTime end4 = now().plusDays(25);

        booking1.setStart(start1);
        booking2.setStart(start2);
        booking3.setStart(start3);
        booking4.setStart(start4);
        booking1.setEnd(end1);
        booking2.setEnd(end2);
        booking3.setEnd(end3);
        booking4.setEnd(end4);

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
        booking4 = bookingRepository.save(booking4);

        List<Booking> nextBookingsForItems = bookingRepository.findNextBookingsForItems(
                List.of(item1.getId(), item2.getId()), status.toString(), now());

        assertAll(
                () -> assertNotNull(nextBookingsForItems),
                () -> assertEquals(2, nextBookingsForItems.size()),
                () -> assertThat(nextBookingsForItems, hasItem(booking2)),
                () -> assertThat(nextBookingsForItems, hasItem(booking4))
        );
    }

    @Test
    void findAllByBooker_Id() {
        LocalDateTime start1 = now().minusDays(10);
        LocalDateTime start2 = now().plusDays(20);
        LocalDateTime end1 = now().plusDays(10);
        LocalDateTime end2 = now().plusDays(30);

        LocalDateTime start3 = now().minusDays(5);
        LocalDateTime start4 = now().plusDays(15);
        LocalDateTime end3 = now().plusDays(15);
        LocalDateTime end4 = now().plusDays(25);

        booking1.setStart(start1);
        booking2.setStart(start2);
        booking3.setStart(start3);
        booking4.setStart(start4);
        booking1.setEnd(end1);
        booking2.setEnd(end2);
        booking3.setEnd(end3);
        booking4.setEnd(end4);

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
        booking4 = bookingRepository.save(booking4);

        List<Booking> ownerBookings = bookingRepository.findAllByBooker_Id(21L, pageable );
        assertAll(
                () -> assertNotNull(ownerBookings),
                () -> assertEquals(2, ownerBookings.size()),
                () -> assertThat(ownerBookings, hasItem(booking1)),
                () -> assertThat(ownerBookings, hasItem(booking3))
        );
    }

    @Test
    void findAllByBooker_IdAndEndBefore() {
        LocalDateTime start1 = now().minusDays(10);
        LocalDateTime start2 = now().plusDays(20);
        LocalDateTime end1 = now().plusDays(10);
        LocalDateTime end2 = now().plusDays(30);

        LocalDateTime start3 = now().minusDays(5);
        LocalDateTime start4 = now().plusDays(15);
        LocalDateTime end3 = now().plusDays(15);
        LocalDateTime end4 = now().plusDays(25);

        booking1.setStart(start1);
        booking2.setStart(start2);
        booking3.setStart(start3);
        booking4.setStart(start4);
        booking1.setEnd(end1);
        booking2.setEnd(end2);
        booking3.setEnd(end3);
        booking4.setEnd(end4);

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
        booking4 = bookingRepository.save(booking4);
        pageable = PageRequest.of(0, 25, Sort.Direction.DESC, "id");

        List<Booking> ownerBookings1 = bookingRepository.findAllByBooker_IdAndEndBefore(1L, now().plusDays(10),
                pageable );
        assertAll(
                () -> assertNotNull(ownerBookings1),
                () -> assertEquals(1, ownerBookings1.size()),
                () -> assertThat(ownerBookings1, hasItem(booking1))
        );
    }

    @Test
    void findAllByBooker_IdAndStartAfter() {
        LocalDateTime start1 = now().minusDays(10);
        LocalDateTime start2 = now().plusDays(20);
        LocalDateTime end1 = now().plusDays(10);
        LocalDateTime end2 = now().plusDays(30);

        LocalDateTime start3 = now().minusDays(5);
        LocalDateTime start4 = now().plusDays(15);
        LocalDateTime end3 = now().plusDays(15);
        LocalDateTime end4 = now().plusDays(25);

        booking1.setStart(start1);
        booking2.setStart(start2);
        booking3.setStart(start3);
        booking4.setStart(start4);
        booking1.setEnd(end1);
        booking2.setEnd(end2);
        booking3.setEnd(end3);
        booking4.setEnd(end4);

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
        booking4 = bookingRepository.save(booking4);
        pageable = PageRequest.of(0, 25, Sort.Direction.DESC, "id");

        List<Booking> ownerBookings = bookingRepository.findAllByBooker_IdAndStartAfter(13L, now().minusDays(6),
                pageable );
        assertAll(
                () -> assertNotNull(ownerBookings),
                () -> assertEquals(1, ownerBookings.size()),
                () -> assertThat(ownerBookings, hasItem(booking3))
        );
    }

    @Test
    void findAllByBooker_IdAndStartBeforeAndEndAfter() {
        LocalDateTime start1 = now().minusDays(10);
        LocalDateTime start2 = now().plusDays(20);
        LocalDateTime end1 = now().plusDays(10);
        LocalDateTime end2 = now().plusDays(30);

        LocalDateTime start3 = now().minusDays(5);
        LocalDateTime start4 = now().plusDays(15);
        LocalDateTime end3 = now().plusDays(15);
        LocalDateTime end4 = now().plusDays(25);

        booking1.setStart(start1);
        booking2.setStart(start2);
        booking3.setStart(start3);
        booking4.setStart(start4);
        booking1.setEnd(end1);
        booking2.setEnd(end2);
        booking3.setEnd(end3);
        booking4.setEnd(end4);

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
        booking4 = bookingRepository.save(booking4);
        pageable = PageRequest.of(0, 25, Sort.Direction.DESC, "id");

        List<Booking> ownerBookings = bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfter(25L,
                now().minusDays(4), now().plusDays(5), pageable );
        assertAll(
                () -> assertNotNull(ownerBookings),
                () -> assertEquals(2, ownerBookings.size()),
                () -> assertThat(ownerBookings, hasItem(booking1)),
                () -> assertThat(ownerBookings, hasItem(booking3))
        );
    }

    @Test
    void findAllByBooker_IdAndStatus() {
        LocalDateTime start1 = now().minusDays(10);
        LocalDateTime start2 = now().plusDays(20);
        LocalDateTime end1 = now().plusDays(10);
        LocalDateTime end2 = now().plusDays(30);

        LocalDateTime start3 = now().minusDays(5);
        LocalDateTime start4 = now().plusDays(15);
        LocalDateTime end3 = now().plusDays(15);
        LocalDateTime end4 = now().plusDays(25);

        booking1.setStart(start1);
        booking2.setStart(start2);
        booking3.setStart(start3);
        booking4.setStart(start4);
        booking1.setEnd(end1);
        booking2.setEnd(end2);
        booking3.setEnd(end3);
        booking4.setEnd(end4);

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
        booking4 = bookingRepository.save(booking4);
        pageable = PageRequest.of(0, 25, Sort.Direction.DESC, "id");

        List<Booking> ownerBookings = bookingRepository.findAllByBooker_IdAndStatus(17L,
                Status.APPROVED, pageable );
        assertAll(
                () -> assertNotNull(ownerBookings),
                () -> assertEquals(2, ownerBookings.size()),
                () -> assertThat(ownerBookings, hasItem(booking1)),
                () -> assertThat(ownerBookings, hasItem(booking3))
        );
    }


}
