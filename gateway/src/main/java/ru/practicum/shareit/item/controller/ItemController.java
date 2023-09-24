package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String HEADER = "X-Sharer-User-Id";
    private static final String DEFAULT_SIZE = "25";
    private static final String DEFAULT_FROM = "0";

    @PostMapping
    public ResponseEntity<Object> postItem(@RequestHeader(value = HEADER) long ownerId,
                                           @Valid @RequestBody ItemDto itemDto) {
        log.info("получен POST запрос на добавление новой вещи с body={}, ownerId={}", itemDto, ownerId);
        return itemClient.addItem(ownerId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestHeader(value = HEADER) long userId,
                                              @Valid @RequestBody CommentInputDto commentInputDto,
                                              @PathVariable(value = "itemId") long itemId) {
        log.info("получен POST запрос на добавление нового комментария с body={}, itemId={} , userId={}",
                commentInputDto,
                itemId,
                userId);
        return itemClient.addComment(itemId, userId, commentInputDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestHeader(value = HEADER) long ownerId,
                                             @RequestBody ItemDto itemDto,
                                             @PathVariable(value = "id") long itemId) {
        log.info("получен PATCH запрос на обновление вещи с id={}, requestBody={}, ownerId={}",
                itemId, itemDto, ownerId);
        return itemClient.updateItem(ownerId, itemId, itemDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@RequestHeader(value = HEADER) long userId,
                                              @PathVariable(value = "id") long id) {
        log.info("получен GET запрос на показ веши с id={}", id);
        return itemClient.getItemById(userId, id);
    }

    @GetMapping("")
    public ResponseEntity<Object> getAllOwnersItems(@RequestHeader(value = HEADER) long ownerId,
                                                    @RequestParam(value = "from",
                                                            defaultValue = DEFAULT_FROM) @PositiveOrZero int from,
                                                    @RequestParam(value = "size",
                                                            defaultValue = DEFAULT_SIZE) @Positive int size
    ) {
        log.info("получен GET запрос на показ всех вещей владельца с ownerId={} from={} size={}", ownerId, from, size);
        return itemClient.getAllOwnersItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItems(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                            @RequestParam(value = "text") String text,
                                            @RequestParam(value = "from", defaultValue = DEFAULT_FROM) @PositiveOrZero int from,
                                            @RequestParam(value = "size", defaultValue = DEFAULT_SIZE) @Positive int size
    ) {
        log.info("получен GET запрос найти все вещи с текстом text={} from={} size={}", text, from, size);
        return itemClient.findItems(userId, text, from, size);
    }
}
