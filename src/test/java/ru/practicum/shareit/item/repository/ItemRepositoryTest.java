package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private User requester;
    private User notRequester;
    private User owner1;
    private User owner2;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private ItemRequest itemRequest3;
    private ItemRequest itemRequest4;
    private PageRequest pageable;
    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;

    @BeforeEach
    void setUp() {
        requester = User.builder()
                .name("Маша")
                .email("masha@mail.ru")
                .build();
        notRequester = User.builder()
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

        userRepository.save(requester);
        userRepository.save(notRequester);
        userRepository.save(owner1);
        userRepository.save(owner2);

        itemRequest1 = new ItemRequest(0L, "description1", now().minusMinutes(1), requester);
        itemRequest2 = new ItemRequest(0L, "description2", now().minusMinutes(2), requester);
        itemRequest3 = new ItemRequest(0L, "description3", now().minusMinutes(3), requester);
        itemRequest4 = new ItemRequest(0L, "description4", now().minusMinutes(4), requester);

        itemRequest1 = requestRepository.save(itemRequest1);
        itemRequest2 = requestRepository.save(itemRequest2);
        itemRequest3 = requestRepository.save(itemRequest3);
        itemRequest4 = requestRepository.save(itemRequest4);

        item1 = new Item(0L, "item1", "description1", true, owner1, itemRequest1);
        item2 = new Item(0L, "item2", "description2", true, owner2, itemRequest2);
        item3 = new Item(0L, "item3", "description3", true, owner2, itemRequest1);
        item4 = new Item(0L, "item4", "description4", true, owner1, itemRequest3);

        item1 = itemRepository.save(item1);
        item2 = itemRepository.save(item2);
        item3 = itemRepository.save(item3);
        item4 = itemRepository.save(item4);

        pageable = PageRequest.of(0, 25, Sort.Direction.DESC, "id");
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        requestRepository.deleteAll();
    }

    @Test
    void findAllByOwner() {
        pageable = PageRequest.of(0, 25, Sort.Direction.DESC, "id");
        List<Item> allByOwner = itemRepository.findAllByOwner(owner1, pageable);

        assertAll(
                () -> assertNotNull(allByOwner),
                () -> assertEquals(2, allByOwner.size()),
                () -> assertThat(allByOwner, hasItem(item1)),
                () -> assertThat(allByOwner, hasItem(item4))
        );
    }

    @Test
    void findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs() {
        String text = "item";
        List<Item> allFound1 =
                itemRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(
                        text,
                        text,
                        true,
                        pageable);

        assertAll(
                () -> assertNotNull(allFound1),
                () -> assertEquals(4, allFound1.size()),
                () -> assertThat(allFound1, hasItem(item1)),
                () -> assertThat(allFound1, hasItem(item2)),
                () -> assertThat(allFound1, hasItem(item3)),
                () -> assertThat(allFound1, hasItem(item4))
        );

        text = "Item";
        List<Item> allFound2 =
                itemRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(
                        text,
                        text,
                        true,
                        pageable);

        assertAll(
                () -> assertNotNull(allFound2),
                () -> assertEquals(4, allFound2.size()),
                () -> assertThat(allFound2, hasItem(item1)),
                () -> assertThat(allFound2, hasItem(item2)),
                () -> assertThat(allFound2, hasItem(item3)),
                () -> assertThat(allFound2, hasItem(item4))
        );

        text = "cripT";
        List<Item> allFound3 =
                itemRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(
                        text,
                        text,
                        true,
                        pageable);

        assertAll(
                () -> assertNotNull(allFound3),
                () -> assertEquals(4, allFound3.size()),
                () -> assertThat(allFound3, hasItem(item1)),
                () -> assertThat(allFound3, hasItem(item2)),
                () -> assertThat(allFound3, hasItem(item3)),
                () -> assertThat(allFound3, hasItem(item4))
        );

        text = "ON3";
        List<Item> allFound4 =
                itemRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(
                        text,
                        text,
                        true,
                        pageable);

        assertAll(
                () -> assertNotNull(allFound4),
                () -> assertEquals(1, allFound4.size()),
                () -> assertThat(allFound4, hasItem(item3))
        );
    }

    @Test
    void findAllByRequest_Id() {
        List<Item> allByRequestId1 =
                itemRepository.findAllByRequest_Id(itemRequest1.getId()).orElse(Collections.emptyList());

        assertAll(
                () -> assertNotNull(allByRequestId1),
                () -> assertEquals(2, allByRequestId1.size()),
                () -> assertThat(allByRequestId1, hasItem(item1)),
                () -> assertThat(allByRequestId1, hasItem(item3))
        );

        List<Item> allByRequestId2 =
                itemRepository.findAllByRequest_Id(itemRequest2.getId()).orElse(Collections.emptyList());

        assertAll(
                () -> assertNotNull(allByRequestId2),
                () -> assertEquals(1, allByRequestId2.size()),
                () -> assertThat(allByRequestId2, hasItem(item2))
        );

        List<Item> allByRequestId4 =
                itemRepository.findAllByRequest_Id(itemRequest4.getId()).orElse(Collections.emptyList());

        assertAll(
                () -> assertNotNull(allByRequestId4),
                () -> assertEquals(0, allByRequestId4.size())
        );
    }

    @Test
    void findAllByRequest_IdIn() {
        List<Long> requestsId1 = List.of(itemRequest1.getId(), itemRequest2.getId(), itemRequest3.getId());
        List<Item> findAllByRequestIdIn1 =
                itemRepository.findAllByRequest_IdIn(requestsId1).orElse(Collections.emptyList());

        assertAll(
                () -> assertNotNull(findAllByRequestIdIn1),
                () -> assertEquals(4, findAllByRequestIdIn1.size()),
                () -> assertThat(findAllByRequestIdIn1, hasItem(item1)),
                () -> assertThat(findAllByRequestIdIn1, hasItem(item2)),
                () -> assertThat(findAllByRequestIdIn1, hasItem(item3)),
                () -> assertThat(findAllByRequestIdIn1, hasItem(item4))
        );

        List<Long> requestsId2 = List.of(itemRequest2.getId(), itemRequest3.getId());
        List<Item> findAllByRequestIdIn2 =
                itemRepository.findAllByRequest_IdIn(requestsId2).orElse(Collections.emptyList());

        assertAll(
                () -> assertNotNull(findAllByRequestIdIn2),
                () -> assertEquals(2, findAllByRequestIdIn2.size()),
                () -> assertThat(findAllByRequestIdIn2, hasItem(item2)),
                () -> assertThat(findAllByRequestIdIn2, hasItem(item4))
        );

        List<Long> requestsId3 = List.of(itemRequest4.getId());
        List<Item> findAllByRequestIdIn3 =
                itemRepository.findAllByRequest_IdIn(requestsId3).orElse(Collections.emptyList());

        assertAll(
                () -> assertNotNull(findAllByRequestIdIn3),
                () -> assertEquals(0, findAllByRequestIdIn3.size())
        );
    }
}
