package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

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


@WebMvcTest(controllers = BookingController.class)
class BookingControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private static BookingInputDto bookingInputDto;
    private static BookingInputDto badBookingDto;
    private static BookingOutputDto bookingOutputDto;
    private static final String HEADER = "X-Sharer-User-Id";

    @BeforeAll
    static void setUp() {
        bookingInputDto = new BookingInputDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), 1L);
        badBookingDto = new BookingInputDto(LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(1), 1L);
        bookingOutputDto = new BookingOutputDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                new ItemDto(),
                new UserDto(1L, "booker", "booker@mail.ru"),
                Status.WAITING
        );
    }

    @Test
    @SneakyThrows
    void addBooking() {
        when(bookingService.addBooking(any(BookingInputDto.class), anyLong())).thenReturn(bookingOutputDto);

        mvc.perform(post("/bookings")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutputDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingOutputDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingOutputDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status",
                        is(bookingOutputDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id",
                        is(bookingOutputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id",
                        is(bookingOutputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name",
                        is(bookingOutputDto.getItem().getName())));

        mvc.perform(post("/bookings")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(badBookingDto)))
                .andExpect(status().isBadRequest());

        when(bookingService.addBooking(any(BookingInputDto.class), anyLong()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(post("/bookings", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isNotFound());

        verify(bookingService, times(2)).addBooking(any(BookingInputDto.class), anyLong());
    }

    @Test
    @SneakyThrows
    void patchBooking() {
        bookingOutputDto.setStatus(Status.APPROVED);
        when(bookingService.updateBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingOutputDto);

        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutputDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingOutputDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingOutputDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status",
                        is(bookingOutputDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id",
                        is(bookingOutputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id",
                        is(bookingOutputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name",
                        is(bookingOutputDto.getItem().getName())));

        when(bookingService.updateBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(ElementNotFoundException.class);

        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(2)).updateBooking(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @SneakyThrows
    void getBooking() {
        when(bookingService.getBookingByIdAndBookerId(anyLong(), anyLong())).thenReturn(bookingOutputDto);

        mvc.perform(get("/bookings/{bookingId}", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutputDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingOutputDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingOutputDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status",
                        is(bookingOutputDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id",
                        is(bookingOutputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id",
                        is(bookingOutputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name",
                        is(bookingOutputDto.getItem().getName())));
    }

    @Test
    @SneakyThrows
    void getAllUsersBooking() {
        when(bookingService.getAllUsersBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutputDto));

        mvc.perform(get("/bookings")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingOutputDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start",
                        is(bookingOutputDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].end",
                        is(bookingOutputDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].status",
                        is(bookingOutputDto.getStatus().toString())))
                .andExpect(jsonPath("$.[0].booker.id",
                        is(bookingOutputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.id",
                        is(bookingOutputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.name",
                        is(bookingOutputDto.getItem().getName())));

        mvc.perform(get("/bookings")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "xyz")
                        .header(HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getAllOwnersBooking() {
        when(bookingService.getAllOwnersBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutputDto));

        mvc.perform(get("/bookings/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingOutputDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start",
                        is(bookingOutputDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].end",
                        is(bookingOutputDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].status",
                        is(bookingOutputDto.getStatus().toString())))
                .andExpect(jsonPath("$.[0].booker.id",
                        is(bookingOutputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.id",
                        is(bookingOutputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.name",
                        is(bookingOutputDto.getItem().getName())));

        mvc.perform(get("/bookings/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "xyz")
                        .header(HEADER, 1))
                .andExpect(status().isOk());
    }
}
