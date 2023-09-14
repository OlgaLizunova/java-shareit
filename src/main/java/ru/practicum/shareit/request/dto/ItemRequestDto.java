package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
public class ItemRequestDto {
    private long id;
    @NotNull
    private String description;
    @NotNull
    private long requestor;
    private LocalDateTime created;
}
