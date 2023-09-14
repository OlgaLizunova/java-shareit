package ru.practicum.shareit.item.dto.mapper;

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

import java.util.List;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface ItemMapper {

    @Mapping(target = "owner", ignore = true)
    Item toItem(ItemDto itemDto);

    ItemDto toItemDto(Item item);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "lastBooking", qualifiedByName = ("ToBookingForItemDto"))
    @Mapping(target = "nextBooking", source = "nextBooking", qualifiedByName = ("ToBookingForItemDto"))
    ItemWithCommentsOutputDto toItemWithCommentDto(Item item, Booking lastBooking, Booking nextBooking);

    @Named("ToBookingForItemDto")
    static BookingForItemDto toBookingForItemDto(Booking booking) {
        return BookingMapper.INSTANCE.toBookingForItemDto(booking);
    }

    @Mapping(target = "owner", ignore = true)
    List<ItemDto> mapDto(List<Item> items);

}
