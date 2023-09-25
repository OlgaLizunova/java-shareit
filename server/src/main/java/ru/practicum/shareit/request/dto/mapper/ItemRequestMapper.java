package ru.practicum.shareit.request.dto.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class, UserMapper.class})
public interface ItemRequestMapper {
    ItemRequestOutDto toItemRequestOutDto(ItemRequest itemRequest);

    ItemRequest toItemRequest(ItemRequestInputDto itemRequestInputDto);


    List<ItemRequestOutDto> map(List<ItemRequest> itemRequests);
}
