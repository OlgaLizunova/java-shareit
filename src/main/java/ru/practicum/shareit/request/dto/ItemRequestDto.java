package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    private long id;
    @NotNull
    private String description;
    @NotNull
    private long requestor;
    private LocalDateTime created;

    public ItemRequestDto(String description, long requestor, LocalDateTime created) {
        this.description = description;
        this.requestor = requestor;
        this.created = created;
    }
}
