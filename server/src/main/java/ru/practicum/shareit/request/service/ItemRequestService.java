package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestOutDto addItemRequest(ItemRequestInputDto itemRequestInputDto, long requesterId);

    ItemRequestOutDto getItemRequestById(long requestId, long userId);

    List<ItemRequestOutDto> getAllRequestersItemRequests(long ownerId, int from, int size);

    List<ItemRequestOutDto> getAllItemRequests(long userId, int from, int size);
}
