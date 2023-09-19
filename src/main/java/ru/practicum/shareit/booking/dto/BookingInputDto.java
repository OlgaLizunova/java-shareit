package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingInputDto {
    @NotNull
    @FutureOrPresent(message = "Start booking may be only in present or future")
    private LocalDateTime start;
    @NotNull
    @Future(message = "End of booking may be only in future")
    private LocalDateTime end;
    @NotNull
    private long itemId;
}
