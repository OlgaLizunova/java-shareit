package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotAvailableException;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.item.comment.dto.CommentInputDto;
import ru.practicum.shareit.item.comment.dto.CommentOutputDto;
import ru.practicum.shareit.item.comment.dto.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsOutputDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @InjectMocks
    ItemServiceImpl itemService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ItemMapper itemMapper;
    @Mock
    CommentMapper commentMapper;

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
    final Status status = Status.APPROVED;
    final int from = 0;
    final int size = 10;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;


    @Test
    void addItem_whenUserFound_thenItemAdd() {
        User owner = new User(userId, userName, email);
        Item newItem = new Item(0, itemName, itemDescription, Boolean.TRUE, null, null);
        ItemDto newItemDto = new ItemDto(0, itemName, itemDescription, Boolean.TRUE, null);
        Item addedItem = new Item(itemId, itemName, itemDescription, Boolean.TRUE, owner, null);
        ItemDto addedItemDto = new ItemDto(itemId, itemName, itemDescription, Boolean.TRUE, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(newItemDto)).thenReturn(newItem);
        when(itemRepository.save(newItem)).thenReturn(addedItem);
        when(itemMapper.toItemDto(addedItem)).thenReturn(addedItemDto);

        ItemDto resultItemDto = itemService.addItem(newItemDto, userId);

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item itemInSave = itemArgumentCaptor.getValue();

        InOrder inOrder = inOrder(userRepository, itemMapper, itemRepository);
        inOrder.verify(userRepository).findById(userId);
        inOrder.verify(itemMapper).toItem(newItemDto);
        inOrder.verify(itemRepository).save(itemInSave);
        inOrder.verify(itemMapper).toItemDto(addedItem);
        assertEquals(addedItem.getOwner(), itemInSave.getOwner());
        assertEquals(addedItemDto, resultItemDto);
    }

    @Test
    void addItem_whenUserNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> itemService.addItem(new ItemDto(), userId));
    }

    @Test
    void getItemById_whenIsItemAndRequiredFromOwner_thenReturnWithBookings() {
        long ownerId = userId + 10;
        long bookerId = userId;
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        User owner = new User(ownerId, userName, email);
        Item item = new Item(itemId, itemName, itemDescription, Boolean.TRUE, owner, null);
        Booking lastBooking = new Booking(bookingId, start, end, item, booker, status);
        Booking nextBooking = new Booking(bookingId + 1, start.plusDays(10), end.plusDays(20), item, booker, status);
        BookingForItemDto lastBookingDto = new BookingForItemDto(
                lastBooking.getId(),
                lastBooking.getStart(),
                lastBooking.getEnd(),
                lastBooking.getBooker().getId(),
                lastBooking.getStatus()
        );

        BookingForItemDto nextBookingDto = new BookingForItemDto(
                nextBooking.getId(),
                nextBooking.getStart(),
                nextBooking.getEnd(),
                nextBooking.getBooker().getId(),
                nextBooking.getStatus()
        );

        User commentator1 = new User(5L, "Commentator1", "Commentator1@mail.ru");
        User commentator2 = new User(6L, "Commentator2", "Commentator2@mail.ru");
        Comment comment1 = new Comment(10L, "Comment1", dateTime.minusHours(1), item, commentator1);
        Comment comment2 = new Comment(11L, "Comment2", dateTime.minusHours(2), item, commentator2);
        CommentOutputDto commentOutputDto1 = new CommentOutputDto(
                comment1.getId(),
                comment1.getText(),
                comment1.getCreated(),
                comment1.getAuthor().getName()
        );
        CommentOutputDto commentOutputDto2 = new CommentOutputDto(
                comment2.getId(),
                comment2.getText(),
                comment2.getCreated(),
                comment2.getAuthor().getName()
        );

        List<Comment> comments = List.of(comment1, comment2);
        List<CommentOutputDto> commentsOutputDto = List.of(commentOutputDto1, commentOutputDto2);

        ItemWithCommentsOutputDto afterInputBookingsDto = new ItemWithCommentsOutputDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBookingDto,
                nextBookingDto,
                null
        );

        ItemWithCommentsOutputDto expectedResultDto = new ItemWithCommentsOutputDto(
                itemId,
                itemName,
                itemDescription,
                true,
                lastBookingDto,
                nextBookingDto,
                commentsOutputDto
        );

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBookingsForItems(eq(List.of(itemId)), anyString(), any()))
                .thenReturn(List.of(lastBooking));

        when(bookingRepository.findNextBookingsForItems(eq(List.of(itemId)), anyString(), any()))
                .thenReturn(List.of(nextBooking));

        when(itemMapper.toItemWithCommentDto(item, lastBooking, nextBooking)).thenReturn(afterInputBookingsDto);
        when(commentRepository.findAllInItemId(List.of(itemId))).thenReturn(comments);
        when(commentMapper.outputMap(comments)).thenReturn(commentsOutputDto);

        ItemWithCommentsOutputDto resultDto = itemService.getItemById(itemId, ownerId);


        InOrder inOrder = inOrder(itemRepository, bookingRepository, itemMapper, commentRepository, commentMapper);
        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(bookingRepository).findLastBookingsForItems(eq(List.of(itemId)), anyString(), any());
        inOrder.verify(bookingRepository).findNextBookingsForItems(eq(List.of(itemId)), anyString(), any());
        inOrder.verify(itemMapper).toItemWithCommentDto(item, lastBooking, nextBooking);
        inOrder.verify(commentRepository).findAllInItemId(List.of(itemId));
        inOrder.verify(commentMapper).outputMap(comments);
        assertEquals(expectedResultDto, resultDto);
    }

    @Test
    void getItemById_whenIsItemAndRequiredFromNotOwner_thenReturnWithNullInBookingsFields() {
        long ownerId = userId + 10;
        User owner = new User(ownerId, userName, email);
        Item item = new Item(itemId, itemName, itemDescription, Boolean.TRUE, owner, null);
        User commentator1 = new User(5L, "Commentator1", "Commentator1@mail.ru");
        User commentator2 = new User(6L, "Commentator2", "Commentator2@mail.ru");
        Comment comment1 = new Comment(10L, "Comment1", dateTime.minusHours(1), item, commentator1);
        Comment comment2 = new Comment(11L, "Comment2", dateTime.minusHours(2), item, commentator2);
        CommentOutputDto commentOutputDto1 = new CommentOutputDto(
                comment1.getId(),
                comment1.getText(),
                comment1.getCreated(),
                comment1.getAuthor().getName());
        CommentOutputDto commentOutputDto2 = new CommentOutputDto(
                comment2.getId(),
                comment2.getText(),
                comment2.getCreated(),
                comment2.getAuthor().getName()
        );

        List<Comment> comments = List.of(comment1, comment2);
        List<CommentOutputDto> commentsOutputDto = List.of(commentOutputDto1, commentOutputDto2);

        ItemWithCommentsOutputDto nullBookingsDto = new ItemWithCommentsOutputDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                null
        );

        ItemWithCommentsOutputDto expectedResultDto = new ItemWithCommentsOutputDto(
                itemId,
                itemName,
                itemDescription,
                true,
                null,
                null,
                commentsOutputDto
        );

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemWithCommentDto(item, null, null)).thenReturn(nullBookingsDto);
        when(commentRepository.findAllInItemId(List.of(itemId))).thenReturn(comments);
        when(commentMapper.outputMap(comments)).thenReturn(commentsOutputDto);

        ItemWithCommentsOutputDto resultDto = itemService.getItemById(itemId, userId);

        InOrder inOrder = inOrder(itemRepository, itemMapper, commentRepository, commentMapper);
        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(itemMapper).toItemWithCommentDto(item, null, null);
        inOrder.verify(commentRepository).findAllInItemId(List.of(itemId));
        inOrder.verify(commentMapper).outputMap(comments);
        verify(bookingRepository, never()).findLastBookingsForItems(any(), anyString(), any());
        verify(bookingRepository, never()).findNextBookingsForItems(any(), anyString(), any());
        assertEquals(expectedResultDto, resultDto);
    }

    @Test
    void getItemById_whenNotFoundItem_thenNotFoundException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> itemService.getItemById(itemId, userId));
    }

    @Test
    void updateItem_whenNewNameDescAvailable_thenChangeNameDescAvailableOnly() {
        long ownerId = userId + 10;
        long newItemId = itemId + 10;
        String updateItemName = "upd" + itemName;
        String updateDescription = "upd" + itemDescription;
        User owner = new User(ownerId, userName, email);
        ItemDto newItemDto = new ItemDto(newItemId, updateItemName, updateDescription, false, null);
        Item oldItem = new Item(itemId, itemName, itemDescription, Boolean.TRUE, owner, null);
        Item newItem = new Item(
                newItemDto.getId(),
                newItemDto.getName(),
                newItemDto.getDescription(),
                newItemDto.getAvailable(),
                null, null
        );
        Item updatedItem = new Item(
                oldItem.getId(),
                newItemDto.getName(),
                newItemDto.getDescription(),
                newItemDto.getAvailable(),
                oldItem.getOwner(), null
        );
        ItemDto expectedItemDto = new ItemDto(
                updatedItem.getId(),
                updatedItem.getName(),
                updatedItem.getDescription(),
                updatedItem.getAvailable(), null
        );
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(itemMapper.toItem(newItemDto)).thenReturn(newItem);
        when(itemRepository.save(newItem)).thenReturn(updatedItem);
        when(itemMapper.toItemDto(updatedItem)).thenReturn(expectedItemDto);

        ItemDto resultDto = itemService.updateItem(itemId, newItemDto, ownerId);
        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item itemInSave = itemArgumentCaptor.getValue();

        InOrder inOrder = inOrder(userRepository, itemRepository, itemMapper);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(itemMapper).toItem(newItemDto);
        inOrder.verify(itemRepository).save(newItem);
        inOrder.verify(itemMapper).toItemDto(updatedItem);
        assertEquals(expectedItemDto, resultDto);
        assertEquals(updatedItem, itemInSave);
    }

    @Test
    void updateItem_whenNewAvailableOnly_thenChangeAvailableOnly() {
        long ownerId = userId + 10;
        long newItemId = itemId + 10;
        User owner = new User(ownerId, userName, email);
        ItemDto newItemDto = new ItemDto(newItemId, null, null, false, null);
        Item oldItem = new Item(itemId, itemName, itemDescription, Boolean.TRUE, owner, null);
        Item newItem = new Item(
                newItemDto.getId(),
                newItemDto.getName(),
                newItemDto.getDescription(),
                newItemDto.getAvailable(),
                null, null
        );
        Item updatedItem = new Item(
                oldItem.getId(),
                oldItem.getName(),
                oldItem.getDescription(),
                newItemDto.getAvailable(),
                oldItem.getOwner(), null
        );
        ItemDto expectedItemDto = new ItemDto(
                updatedItem.getId(),
                updatedItem.getName(),
                updatedItem.getDescription(),
                updatedItem.getAvailable(), null
        );
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(itemMapper.toItem(newItemDto)).thenReturn(newItem);
        when(itemRepository.save(newItem)).thenReturn(updatedItem);
        when(itemMapper.toItemDto(updatedItem)).thenReturn(expectedItemDto);

        ItemDto resultDto = itemService.updateItem(itemId, newItemDto, ownerId);
        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item itemInSave = itemArgumentCaptor.getValue();

        InOrder inOrder = inOrder(userRepository, itemRepository, itemMapper);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(itemMapper).toItem(newItemDto);
        inOrder.verify(itemRepository).save(newItem);
        inOrder.verify(itemMapper).toItemDto(updatedItem);
        assertEquals(expectedItemDto, resultDto);
        assertEquals(updatedItem, itemInSave);
    }

    @Test
    void updateItem_whenNotFoundOwner_thenNotFoundException() {
        ItemDto newItemDto = new ItemDto();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> itemService.updateItem(itemId, newItemDto, userId));
    }

    @Test
    void updateItem_whenNotFoundItem_thenNotFoundException() {
        ItemDto newItemDto = new ItemDto();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class, () -> itemService.updateItem(itemId, newItemDto, userId));
    }

    @Test
    void updateItem_whenAccessNotOwner_thenNotNotOwnerException() {
        ItemDto newItemDto = new ItemDto();
        long notOwnerId = userId + 10;
        User owner = new User(userId, userName, email);
        Item item = new Item(itemId, itemName, itemDescription, Boolean.TRUE, owner, null);
        when(userRepository.findById(notOwnerId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(NotOwnerException.class,
                () -> itemService.updateItem(itemId, newItemDto, notOwnerId));
    }

    @Test
    void getAllOwnersItems_whenIsItemFromOwner_thenReturnWithBookingsFieldsAndComments() {
        long ownerId = userId + 10;
        long bookerId = userId;
        User booker = new User(bookerId, "booker", "booker@mail.ru");
        User owner = new User(ownerId, userName, email);
        Item item = new Item(itemId, itemName, itemDescription, Boolean.TRUE, owner, null);
        Booking lastBooking = new Booking(bookingId + 10, start.plusDays(10),
                end.plusDays(20), item, booker, status);
        Booking nextBooking = new Booking(bookingId + 20, start.plusDays(30),
                end.plusDays(40), item, booker, status);
        BookingForItemDto lastBookingDto = new BookingForItemDto(lastBooking.getId(), lastBooking.getStart(),
                lastBooking.getEnd(), lastBooking.getBooker().getId(), lastBooking.getStatus());
        BookingForItemDto nextBookingDto = new BookingForItemDto(nextBooking.getId(), nextBooking.getStart(),
                nextBooking.getEnd(), nextBooking.getBooker().getId(), nextBooking.getStatus());
        User commentator1 = new User(5L, "Commentator1", "Commentator1@mail.ru");
        User commentator2 = new User(6L, "Commentator2", "Commentator2@mail.ru");
        Comment comment1 = new Comment(10L, "Comment1", dateTime.minusHours(1), item, commentator1);
        Comment comment2 = new Comment(11L, "Comment2", dateTime.minusHours(2), item, commentator2);
        CommentOutputDto commentOutputDto1 = new CommentOutputDto(
                comment1.getId(),
                comment1.getText(),
                comment1.getCreated(),
                comment1.getAuthor().getName()
        );
        CommentOutputDto commentOutputDto2 = new CommentOutputDto(
                comment2.getId(),
                comment2.getText(),
                comment2.getCreated(),
                comment2.getAuthor().getName()
        );
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentOutputDto> commentsOutputDto = List.of(commentOutputDto1, commentOutputDto2);
        ItemWithCommentsOutputDto afterInputBookingsDto = new ItemWithCommentsOutputDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBookingDto,
                nextBookingDto,
                null
        );
        List<ItemWithCommentsOutputDto> expectedResultDto = List.of(new ItemWithCommentsOutputDto(
                itemId,
                itemName,
                itemDescription,
                true,
                lastBookingDto,
                nextBookingDto,
                commentsOutputDto
        ));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwner(
                owner,
                PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id")))
        ).thenReturn(List.of(item));
        when(bookingRepository.findLastBookingsForItems(eq(List.of(itemId)), anyString(), any()))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findNextBookingsForItems(eq(List.of(itemId)), anyString(), any()))
                .thenReturn(List.of(nextBooking));
        when(itemMapper.toItemWithCommentDto(item, lastBooking, nextBooking)).thenReturn(afterInputBookingsDto);
        when(commentRepository.findAllInItemId(List.of(itemId))).thenReturn(comments);
        when(commentMapper.outputMap(comments)).thenReturn(commentsOutputDto);

        List<ItemWithCommentsOutputDto> resultAllOwnersItems = itemService.getAllOwnersItems(ownerId, from, size);

        InOrder inOrder = inOrder(userRepository, itemRepository, bookingRepository, itemMapper,
                commentRepository, commentMapper);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRepository)
                .findAllByOwner(owner, PageRequest.of(from, size, Sort.Direction.ASC, "id"));
        inOrder.verify(bookingRepository).findLastBookingsForItems(eq(List.of(itemId)), anyString(), any());
        inOrder.verify(bookingRepository).findNextBookingsForItems(eq(List.of(itemId)), anyString(), any());
        inOrder.verify(itemMapper).toItemWithCommentDto(item, lastBooking, nextBooking);
        inOrder.verify(commentRepository).findAllInItemId(List.of(itemId));
        inOrder.verify(commentMapper).outputMap(comments);
        assertEquals(expectedResultDto, resultAllOwnersItems);
    }

    @Test
    void findItems_whenFindTextIsBlank_thenReturnEmptyList() {
        List<ItemDto> resultItems = itemService.findItems("", from, size);
        assertNotNull(resultItems);
        assertTrue(resultItems.isEmpty());
    }

    @Test
    void findItems_whenFindTextIsNotBlank_thenReturnListOfItems() {
        String text = "notBlankText";
        User owner1 = new User(userId, userName, email);
        User owner2 = new User(userId + 1, userName, email);
        Item item1 = new Item(itemId, itemName, itemDescription, Boolean.TRUE, owner1, null);
        Item item2 = new Item(itemId + 1, itemName, itemDescription, Boolean.TRUE, owner2, null);
        ItemDto itemDto1 =
                new ItemDto(item1.getId(), item1.getName(), item1.getDescription(), item1.getAvailable(), null);
        ItemDto itemDto2 =
                new ItemDto(item2.getId(), item2.getName(), item2.getDescription(), item2.getAvailable(), null);
        List<Item> expectedItems = List.of(item1, item2);
        List<ItemDto> expectedItemsDto = List.of(itemDto1, itemDto2);

        when(itemRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(
                anyString(),
                anyString(),
                eq(true),
                eq(PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id")))))
                .thenReturn(List.of(item1, item2));
        when(itemMapper.mapDto(expectedItems)).thenReturn(expectedItemsDto);

        List<ItemDto> resultItemsDto = itemService.findItems(text, from, size);

        InOrder inOrder = inOrder(itemRepository, itemMapper);
        inOrder.verify(itemRepository).findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(anyString(),
                anyString(), anyBoolean(), any());
        inOrder.verify(itemMapper).mapDto(expectedItems);
        assertNotNull(resultItemsDto);
        assertFalse(resultItemsDto.isEmpty());
        assertEquals(expectedItemsDto, resultItemsDto);
    }

    @Test
    void addComment_whenCommentatorNotFound_thenNotFoundException() {
        CommentInputDto commentInputDto = new CommentInputDto();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> itemService.addComment(commentInputDto, itemId, userId));
    }

    @Test
    void addComment_whenItemNotFound_thenNotFoundException() {
        CommentInputDto commentInputDto = new CommentInputDto();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> itemService.addComment(commentInputDto, itemId, userId));
    }

    @Test
    void addComment_whenRightBookingNotFound_thenNotAvailableException() {
        CommentInputDto commentInputDto = new CommentInputDto();
        when(commentMapper.toComment(any())).thenReturn(new Comment());
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(new Item()));
        when(bookingRepository.findFirstByItem_IdAndBooker_IdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(null);

        assertThrows(NotAvailableException.class,
                () -> itemService.addComment(commentInputDto, itemId, userId));
    }
}
