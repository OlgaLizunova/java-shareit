package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    public static final String DEFAULT_FROM = "0";
    public static final String DEFAULT_SIZE = "25";
    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> postItemRequest(@RequestHeader(value = HEADER) long requesterId,
                                                  @Valid @RequestBody ItemRequestInputDto itemRequestInputDto) {
        log.info("получен POST запрос на добавление нового itemRequest with body={}, requesterId={}",
                itemRequestInputDto, requesterId);
        return itemRequestClient.addItemRequest(requesterId, itemRequestInputDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader(value = HEADER) long userId,
                                                     @PathVariable(value = "requestId") long requestId) {
        log.info("получен GET запрос на получение itemRequest с id={}", requestId);
        return itemRequestClient.getItemRequestById(userId, requestId);
    }

    @GetMapping("")
    public ResponseEntity<Object> getAllRequestersItemRequests(@RequestHeader(value = HEADER) long requesterId,
                                                               @RequestParam(value = "from", defaultValue = DEFAULT_FROM) @PositiveOrZero int from,
                                                               @RequestParam(value = "size", defaultValue = DEFAULT_SIZE) @Positive int size) {
        log.info("получен GET запрос на показ всех вещей requesterId={} from={} size={}", requesterId, from, size);
        return itemRequestClient.getAllRequestersItemRequests(requesterId, from, size);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@RequestHeader(value = HEADER) long userId,
                                                     @RequestParam(value = "from", defaultValue = DEFAULT_FROM) @PositiveOrZero int from,
                                                     @RequestParam(value = "size", defaultValue = DEFAULT_SIZE) @Positive int size) {
        log.info("получен GET запрос на показ всех вещей userId={} from={} size={}", userId, from, size);
        return itemRequestClient.getAllItemRequests(userId, from, size);
    }
}
