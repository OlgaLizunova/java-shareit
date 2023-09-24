package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwner(User owner, Pageable pageable);

    List<Item> findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(String text1,
                                                                                                String text2,
                                                                                                boolean isAvailable,
                                                                                                Pageable pageable);

    Optional<List<Item>> findAllByRequest_Id(long requestId);

    Optional<List<Item>> findAllByRequest_IdIn(List<Long> requestsIds);
}
