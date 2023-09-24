package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class BookingForItemDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long bookerId;
    private Status status;
}
