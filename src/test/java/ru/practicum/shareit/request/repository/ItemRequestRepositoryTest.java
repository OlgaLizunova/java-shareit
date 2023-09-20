package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {
    @Autowired
    ItemRequestRepository requestRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    User requester;
    User notRequester;
    User owner;
    ItemRequest itemRequest1;
    ItemRequest itemRequest2;
    ItemRequest itemRequest3;
    ItemRequest itemRequest4;
    PageRequest pageable;

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
        owner = User.builder()
                .name("Дима")
                .email("dima@mail.ru")
                .build();

        userRepository.save(requester);
        userRepository.save(notRequester);
        userRepository.save(owner);

        Item item1 = new Item(0L, "item1", "description1", true, owner, null);
        Item item2 = new Item(0L, "item2", "description2", true, owner, null);
        Item item3 = new Item(1L, "item3", "description3", true, owner, null);
        Item item4 = new Item(2L, "item4", "description4", true, owner, null);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        itemRequest1 = new ItemRequest(0L, "description1", now().minusMinutes(1), requester);
        itemRequest2 = new ItemRequest(1L, "description2", now().minusMinutes(2), notRequester);
        itemRequest3 = new ItemRequest(2L, "description3", now().minusMinutes(3), notRequester);
        itemRequest4 = new ItemRequest(3L, "description4", now().minusMinutes(4), requester);

        itemRequest1 = requestRepository.save(itemRequest1);
        itemRequest2 = requestRepository.save(itemRequest2);
        itemRequest3 = requestRepository.save(itemRequest3);
        itemRequest4 = requestRepository.save(itemRequest4);


        pageable = PageRequest.of(0, 25, Sort.Direction.DESC, "created");
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void findAllByRequester() {
        List<ItemRequest> allByRequester = requestRepository.findAllByRequester(requester, pageable);

        assertAll(
                () -> assertNotNull(allByRequester),
                () -> assertEquals(2, allByRequester.size()),
                () -> assertThat(allByRequester, hasItem(itemRequest1)),
                () -> assertThat(allByRequester, hasItem(itemRequest4))
        );
    }

    @Test
    public void findAllByRequesterNot() {
        List<ItemRequest> allByRequester = requestRepository.findAllByRequester(notRequester, pageable);

        assertAll(
                () -> assertNotNull(allByRequester),
                () -> assertEquals(2, allByRequester.size()),
                () -> assertThat(allByRequester, hasItem(itemRequest2)),
                () -> assertThat(allByRequester, hasItem(itemRequest3))
        );
    }
}
