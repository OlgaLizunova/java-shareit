package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.item.Status;
import java.time.LocalDateTime;

@Builder
@Data
public class BookingDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long item;
    private long booker;
    private Status status;
}
