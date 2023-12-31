package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentInputDto;
import ru.practicum.shareit.item.comment.dto.SavedCommentOutputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsOutputDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String HEADER = "X-Sharer-User-Id";
    private static final String DEFAULT_SIZE = "25";
    private static final String DEFAULT_FROM = "0";

    @PostMapping
    public ItemDto postItem(@RequestHeader(value = HEADER) long ownerId,
                            @RequestBody ItemDto itemDto) {
        log.info("получен POST запрос на добавление новой вещи с body={}, ownerId={}", itemDto, ownerId);
        return itemService.addItem(itemDto, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public SavedCommentOutputDto postComment(@RequestHeader(value = HEADER) long userId,
                                             @RequestBody CommentInputDto commentInputDto,
                                             @PathVariable(value = "itemId") long itemId) {
        log.info("получен POST запрос на добавление нового комментария с body={}, itemId={} , userId={}",
                commentInputDto,
                itemId,
                userId);
        return itemService.addComment(commentInputDto, itemId, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader(value = HEADER) long ownerId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable(value = "id") long itemId) {
        log.info("получен PATCH запрос на обновление вещи с id={}, requestBody={}, ownerId={}",
                itemId, itemDto, ownerId);
        return itemService.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/{id}")
    public ItemWithCommentsOutputDto getItemById(@RequestHeader(value = HEADER) long userId,
                                                 @PathVariable(value = "id") long id) {
        log.info("получен GET запрос на показ веши с id={}", id);
        return itemService.getItemById(id, userId);
    }

    @GetMapping("")
    public List<ItemWithCommentsOutputDto> getAllOwnersItems(@RequestHeader(value = HEADER) long ownerId,
                                                             @RequestParam(value = "from",
                                                                     defaultValue = DEFAULT_FROM) int from,
                                                             @RequestParam(value = "size",
                                                                     defaultValue = DEFAULT_SIZE) int size
    ) {
        log.info("получен GET запрос на показ всех вещей владельца с ownerId={} from={} size={}", ownerId, from, size);
        return itemService.getAllOwnersItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> findItems(@RequestParam(value = "text") String text,
                                   @RequestParam(value = "from", defaultValue = DEFAULT_FROM) int from,
                                   @RequestParam(value = "size", defaultValue = DEFAULT_SIZE) int size
    ) {
        log.info("получен GET запрос найти все вещи с текстом text={} from={} size={}", text, from, size);
        return itemService.findItems(text, from, size);
    }
}
