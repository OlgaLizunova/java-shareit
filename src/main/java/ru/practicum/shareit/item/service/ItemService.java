package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentInputDto;
import ru.practicum.shareit.item.comment.dto.SavedCommentOutputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsOutputDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(ItemDto itemDto, long userId);

    ItemDto updateItem(long itemId, ItemDto itemDto, long ownerId);

    ItemWithCommentsOutputDto getItemById(long id, long userId);

    List<ItemWithCommentsOutputDto> getAllOwnersItems(long ownerId);

    List<ItemDto> findItems(String text);

    SavedCommentOutputDto addComment(CommentInputDto commentInputDto, long itemId, long userId);
}
