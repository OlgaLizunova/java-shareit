package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.item.comment.dto.CommentInputDto;
import ru.practicum.shareit.item.comment.dto.SavedCommentOutputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsOutputDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemService itemService;
    private static ItemDto itemDto;
    private static ItemWithCommentsOutputDto itemWithCommentsOutputDto;
    private static SavedCommentOutputDto commentDto;
    static final LocalDateTime dateTime = LocalDateTime.of(2023, 8, 13, 9, 28, 26);
    private static final String HEADER = "X-Sharer-User-Id";

    @BeforeAll
    static void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .requestId(1L)
                .build();
        itemWithCommentsOutputDto = ItemWithCommentsOutputDto.builder()
                .id(1L)
                .name("item")
                .description("itemWithBookingsAndComments")
                .available(true)
                .lastBooking(new BookingForItemDto(1L,
                        dateTime.minusMonths(10),
                        dateTime.minusMonths(9),
                        1L,
                        Status.APPROVED))
                .nextBooking(new BookingForItemDto(2L,
                        dateTime.minusMonths(1),
                        dateTime.plusDays(9),
                        2L,
                        Status.APPROVED))
                .build();
        commentDto = new SavedCommentOutputDto(1L, "comment", LocalDateTime.now(), itemDto, "ru/practicum/shareit/user");
    }


    @Test
    @SneakyThrows
    void postItem() {
        when(itemService.addItem(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        when(itemService.addItem(any(ItemDto.class), anyLong()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(post("/items", 1)
                        .header(HEADER, 10)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());

        itemDto.setName("");
        mvc.perform(post("/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
        verify(itemService, times(2))
                .addItem(any(ItemDto.class), anyLong());
    }

    @Test
    @SneakyThrows
    void postComment() {
        CommentInputDto commentInputDto = new CommentInputDto();

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(commentInputDto)))
                .andExpect(status().isBadRequest());

        commentInputDto.setText("comment");

        when(itemService.addComment(any(CommentInputDto.class), anyLong(), anyLong()))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(commentInputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created",
                        is(commentDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        when(itemService.addComment(any(CommentInputDto.class), anyLong(), anyLong()))
                .thenThrow(IllegalArgumentException.class);

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(commentInputDto)))
                .andExpect(status().isBadRequest());

        when(itemService.addComment(any(CommentInputDto.class), anyLong(), anyLong()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .header(HEADER, 10)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(commentInputDto)))
                .andExpect(status().isNotFound());

        verify(itemService, times(3))
                .addComment(any(CommentInputDto.class), anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    void updateItem() {
        doThrow(NotOwnerException.class)
                .when(itemService)
                .updateItem(anyLong(), any(ItemDto.class), anyLong());
        mvc.perform(patch("/items/{itemId}", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden());

        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong()))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(patch("/items/{itemId}", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
        verify(itemService, times(3)).updateItem(anyLong(), any(ItemDto.class), anyLong());
    }

    @Test
    @SneakyThrows
    void getItemById() {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemWithCommentsOutputDto);
        mvc.perform(get("/items/{itemId}", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithCommentsOutputDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemWithCommentsOutputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemWithCommentsOutputDto.getAvailable())))
                .andExpect(jsonPath("$.nextBooking.id",
                        is(itemWithCommentsOutputDto.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(itemWithCommentsOutputDto.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.id",
                        is(itemWithCommentsOutputDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId",
                        is(itemWithCommentsOutputDto.getLastBooking().getBookerId()), Long.class));

        when(itemService.getItemById(anyLong(), anyLong())).thenThrow(ElementNotFoundException.class);

        mvc.perform(get("/items/{itemId}", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isNotFound());
        verify(itemService, times(2)).getItemById(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    void getAllOwnersItems() {
        when(itemService.getAllOwnersItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemWithCommentsOutputDto));

        mvc.perform(get("/items")
                        .param("from", "0")
                        .param("size", "25")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemWithCommentsOutputDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemWithCommentsOutputDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemWithCommentsOutputDto.getAvailable())))
                .andExpect(jsonPath("$.[0].nextBooking.id",
                        is(itemWithCommentsOutputDto.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.[0].nextBooking.bookerId",
                        is(itemWithCommentsOutputDto.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.[0].lastBooking.id",
                        is(itemWithCommentsOutputDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.[0].lastBooking.bookerId",
                        is(itemWithCommentsOutputDto.getLastBooking().getBookerId()), Long.class));

        when(itemService.getAllOwnersItems(anyLong(), anyInt(), anyInt()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(get("/items", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isNotFound());
        verify(itemService, times(2)).getAllOwnersItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void findItems() {
        when(itemService.findItems(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .param("text", "name")
                        .param("from", "0")
                        .param("size", "25")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDto.getAvailable())));

        when(itemService.findItems(anyString(), anyInt(), anyInt()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(get("/items/search", 1)
                        .param("text", "text")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isNotFound());
        verify(itemService, times(2)).findItems(anyString(), anyInt(), anyInt());
    }
}
