package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotAvailableException;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.item.comment.dto.CommentInputDto;
import ru.practicum.shareit.item.comment.dto.SavedCommentOutputDto;
import ru.practicum.shareit.item.comment.dto.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsOutputDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.utils.PageRequestUtil;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    @Override
    public ItemDto addItem(ItemDto newItemDto, long userId) {
        User owner = findUserById(userId);
        Item newItem = itemMapper.toItem(newItemDto);

        newItem.setOwner(owner);
        if (newItemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.getReferenceById(newItemDto.getRequestId());
            newItem.setRequest(request);
        }

        Item addedItem = itemRepository.save(newItem);
        log.info("Добавлена вещь ={}", addedItem);

        return itemMapper.toItemDto(addedItem);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemWithCommentsOutputDto getItemById(long id, long userId) {
        Item item = findItemById(id);
        ItemWithCommentsOutputDto itemWithCommentsOutputDto;
        if (item.getOwner().getId() == (userId)) {
            LocalDateTime now = now();
            itemWithCommentsOutputDto = findItemsWithLastAndNextBooking(item, now);
        } else {
            itemWithCommentsOutputDto = itemMapper.toItemWithCommentDto(item, null, null);
        }

        Map<Long, List<Comment>> commentsMap = findCommentsForItem(List.of(id));
        itemWithCommentsOutputDto.setComments(commentMapper.outputMap(commentsMap.get(id)));

        if (itemWithCommentsOutputDto.getComments() == null) {
            itemWithCommentsOutputDto.setComments(Collections.emptyList());
        }

        log.info("Получена вещь ={}, by id={}", itemWithCommentsOutputDto, id);
        return itemWithCommentsOutputDto;

    }

    @Transactional
    @Override
    public ItemDto updateItem(long itemId, ItemDto itemDto, long ownerId) {
        User owner = findUserById(ownerId);
        Item oldItem = findItemById(itemId);

        checkAccess(owner, oldItem);

        Item newItem = itemMapper.toItem(itemDto);

        newItem.setId(itemId);
        newItem.setOwner(owner);
        newItem.setRequest(oldItem.getRequest());

        if (Objects.isNull(newItem.getName())) {
            newItem.setName(oldItem.getName());
        }
        if (Objects.isNull(newItem.getDescription())) {
            newItem.setDescription(oldItem.getDescription());
        }
        if (Objects.isNull(newItem.getAvailable())) {
            newItem.setAvailable(oldItem.getAvailable());
        }

        Item updatedItem = itemRepository.save(newItem);
        log.info("вещь ={} обновлена на новую вещь ={}", oldItem, updatedItem);
        return itemMapper.toItemDto(updatedItem);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemWithCommentsOutputDto> getAllOwnersItems(long ownerId, int from, int size) {
        User owner = findUserById(ownerId);
        List<Item> items = itemRepository.findAllByOwner(owner,
                PageRequestUtil.of(from, size, Sort.by(Sort.Direction.ASC, "id")));
        LocalDateTime now = now();
        List<ItemWithCommentsOutputDto> itemWithCommentsOutputDto = findItemsWithLastAndNextBooking(items, now);

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        Map<Long, List<Comment>> commentsMap = findCommentsForItem(itemIds);
        itemWithCommentsOutputDto.forEach(i -> i.setComments(commentMapper.outputMap(commentsMap.get(i.getId()))));

        log.info("Получен список {} вещей владельца с ownerId={}", itemWithCommentsOutputDto.size(), ownerId);
        return itemWithCommentsOutputDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> findItems(String text, int from, int size) {
        if (text.isBlank()) {
            log.warn("Текст для поиска пустой");
            return Collections.emptyList();
        }
        List<Item> items = itemRepository
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(text,
                        text,
                        true,
                        PageRequestUtil.of(from, size, Sort.by(Sort.Direction.ASC, "id")));
        log.info("Найдены и показаны {} вещи с текстом text={} ", items.size(), text);
        return itemMapper.mapDto(items);
    }

    @Transactional
    @Override
    public SavedCommentOutputDto addComment(CommentInputDto commentInputDto, long itemId, long userId) {
        Comment newComment = commentMapper.toComment(commentInputDto);
        User author = findUserById(userId);
        Item item = findItemById(itemId);
        LocalDateTime now = newComment.getCreated();
        Booking booking = bookingRepository.findFirstByItem_IdAndBooker_IdAndEndBefore(itemId, userId, now);
        if (booking == null) {
            log.error("Пользователь с id={} не может комментировать вещь id={}", userId, itemId);
            throw new NotAvailableException(String.format("Пользователь с id=%d не может комментировать вещь с id=%d",
                    userId, itemId));
        }
        newComment.setItem(item);
        newComment.setAuthor(author);
        Comment addedComment = commentRepository.save(newComment);
        return commentMapper.toSavedCommentOutputDto(addedComment);
    }

    private List<ItemWithCommentsOutputDto> findItemsWithLastAndNextBooking(List<Item> items,
                                                                            LocalDateTime date) {
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());


        List<Booking> lastBookings = bookingRepository.findLastBookingsForItems(itemIds,
                Status.APPROVED.toString(),
                date);
        Map<Long, Booking> lastBookingMap = lastBookings.stream().collect(Collectors.toMap(
                booking -> booking.getItem().getId(), Function.identity()));

        List<Booking> nextBookings = bookingRepository.findNextBookingsForItems(itemIds,
                Status.APPROVED.toString(),
                date);
        Map<Long, Booking> nextBookingMap = nextBookings.stream().collect(Collectors.toMap(
                booking -> booking.getItem().getId(), Function.identity()));

        return items.stream()
                .map(item -> itemMapper.toItemWithCommentDto(item,
                        lastBookingMap.get(item.getId()),
                        nextBookingMap.get(item.getId())))
                .collect(Collectors.toList());
    }

    private Map<Long, List<Comment>> findCommentsForItem(List<Long> itemIds) {
        List<Comment> comments = commentRepository.findAllInItemId(itemIds);
        return comments.stream().collect(
                groupingBy(comment -> comment.getItem().getId()));
    }

    private ItemWithCommentsOutputDto findItemsWithLastAndNextBooking(Item item, LocalDateTime date) {
        return findItemsWithLastAndNextBooking(List.of(item), date).get(0);
    }

    private void checkAccess(User owner, Item itemForUpdate) {
        if (itemForUpdate.getOwner().getId().equals(owner.getId())) {
            return;
        }
        throw new NotOwnerException("Только владелец имеет доступ к вещи");
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new ElementNotFoundException(String.format("Пользователь с id=%d не найден", userId)));
    }

    private Item findItemById(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new ElementNotFoundException(String.format("Вещь с id=%d не найдена", itemId)));
    }
}
