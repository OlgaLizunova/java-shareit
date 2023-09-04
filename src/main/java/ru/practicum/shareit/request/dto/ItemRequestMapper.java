package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.model.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest item) {
        return ItemRequestDto.builder()
                .description(item.getDescription())
                .requestor(item.getRequestor().getId())
                .created(item.getCreated())
                .build();
    }
}
