package ru.practicum.shareit.item.dto.mapper;

import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.comment.dto.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsOutputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.mapper.ItemRequestMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CommentMapper.class, ItemRequestMapper.class})
public interface ItemMapper {

    @Generated
    @Mapping(target = "owner", ignore = true)
    Item toItem(ItemDto itemDto);

    @Generated
    @Mapping(target = "requestId", source = "request.id")
    ItemDto toItemDto(Item item);

    @Generated
    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "lastBooking", qualifiedByName = ("ToBookingForItemDto"))
    @Mapping(target = "nextBooking", source = "nextBooking", qualifiedByName = ("ToBookingForItemDto"))
    ItemWithCommentsOutputDto toItemWithCommentDto(Item item, Booking lastBooking, Booking nextBooking);

    @Generated
    @Named("ToBookingForItemDto")
    static BookingForItemDto toBookingForItemDto(Booking booking) {
        return BookingMapper.INSTANCE.toBookingForItemDto(booking);
    }

    @Generated
    @Mapping(target = "owner", ignore = true)
    List<ItemDto> mapDto(List<Item> items);

}
