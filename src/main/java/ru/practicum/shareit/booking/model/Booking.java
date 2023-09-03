package ru.practicum.shareit.booking.model;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.user.model.User;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class Booking {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    @NotNull
    private Item item;
    @NotNull
    private User booker;
    private Status status;
}

