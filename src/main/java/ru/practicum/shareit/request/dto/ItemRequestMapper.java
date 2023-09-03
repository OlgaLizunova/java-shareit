package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.model.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest item) {
        return new ItemRequestDto(
                item.getDescription(),
                item.getRequestor().getId(),
                item.getCreated());
    }
}
