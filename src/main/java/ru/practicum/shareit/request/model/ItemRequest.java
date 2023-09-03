package ru.practicum.shareit.request.model;

import lombok.Data;
import ru.practicum.shareit.user.model.User;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private long id;
    @NotNull
    private String description;
    @NotNull
    private User requestor;
    private LocalDateTime created;
}
