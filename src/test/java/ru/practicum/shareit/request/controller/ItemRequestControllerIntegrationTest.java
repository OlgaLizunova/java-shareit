package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    ItemRequestService itemRequestService;
    private static ItemRequestInputDto itemRequestInputDto;
    private static ItemRequestOutDto itemRequestOutDto;
    private static final String HEADER = "X-Sharer-User-Id";

    @BeforeAll
    static void setUp() {
        itemRequestInputDto = new ItemRequestInputDto();
        itemRequestInputDto.setDescription("ерунда");

        LocalDateTime date = LocalDateTime.of(2023, 8, 13, 13, 49, 54);

        itemRequestOutDto = ItemRequestOutDto.builder()
                .id(1L)
                .description("ерунда")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    @SneakyThrows
    void postItemRequest() {
        when(itemRequestService.addItemRequest(any(ItemRequestInputDto.class), anyLong()))
                .thenReturn(itemRequestOutDto);
        mvc.perform(post("/requests")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(itemRequestOutDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.description",
                        is(itemRequestOutDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestOutDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        mvc.perform(post("/requests")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(new ItemRequestInputDto())))
                .andExpect(status().isBadRequest());
        verify(itemRequestService, times(1))
                .addItemRequest(any(ItemRequestInputDto.class), anyLong());
    }

    @Test
    @SneakyThrows
    void getItemRequestById() {
        when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestOutDto);

        mvc.perform(get("/requests/{requestId}", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.description",
                        is(itemRequestOutDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestOutDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
        verify(itemRequestService, times(1)).getItemRequestById(anyLong(), anyLong());

        when(itemRequestService.getItemRequestById(anyLong(), anyLong())).thenThrow(ElementNotFoundException.class);

        mvc.perform(get("/requests/{requestId}", 1)
                        .header(HEADER, 10)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemRequestService, times(2)).getItemRequestById(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    void getAllRequestersItemRequests() {
        when(itemRequestService.getAllRequestersItemRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestOutDto));

        mvc.perform(get("/requests")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description",
                        is(itemRequestOutDto.getDescription())))
                .andExpect(jsonPath("$.[0].created",
                        is(itemRequestOutDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(itemRequestService, times(1))
                .getAllRequestersItemRequests(anyLong(), anyInt(), anyInt());

        when(itemRequestService.getAllRequestersItemRequests(anyLong(), anyInt(), anyInt()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(get("/requests", 1)
                        .header(HEADER, 10)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemRequestService, times(2))
                .getAllRequestersItemRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllItemRequests() {
        when(itemRequestService.getAllItemRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestOutDto));

        mvc.perform(get("/requests/all")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description",
                        is(itemRequestOutDto.getDescription())))
                .andExpect(jsonPath("$.[0].created",
                        is(itemRequestOutDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
        verify(itemRequestService, times(1))
                .getAllItemRequests(anyLong(), anyInt(), anyInt());

        when(itemRequestService.getAllItemRequests(anyLong(), anyInt(), anyInt()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(get("/requests/all", 1)
                        .header(HEADER, 10)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemRequestService, times(2))
                .getAllItemRequests(anyLong(), anyInt(), anyInt());
    }
}
