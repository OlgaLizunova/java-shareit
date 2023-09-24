package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    public static final String DEFAULT_FROM = "0";
    public static final String DEFAULT_SIZE = "25";
    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestOutDto postItemRequest(@RequestHeader(value = HEADER) long requesterId,
                                             @RequestBody ItemRequestInputDto itemRequestInputDto) {
        log.info("получен POST запрос на добавление нового itemRequest with body={}, requesterId={}",
                itemRequestInputDto, requesterId);
        return itemRequestService.addItemRequest(itemRequestInputDto, requesterId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestOutDto getItemRequestById(@RequestHeader(value = HEADER) long userId,
                                                @PathVariable(value = "requestId") long requestId) {
        log.info("получен GET запрос на получение itemRequest с id={}", requestId);
        return itemRequestService.getItemRequestById(requestId, userId);
    }

    @GetMapping("")
    public List<ItemRequestOutDto> getAllRequestersItemRequests(@RequestHeader(value = HEADER) long requesterId,
                                                                @RequestParam(value = "from", defaultValue = DEFAULT_FROM) @PositiveOrZero int from,
                                                                @RequestParam(value = "size", defaultValue = DEFAULT_SIZE) @Positive int size) {
        log.info("получен GET запрос на показ всех вещей requesterId={} from={} size={}", requesterId, from, size);
        return itemRequestService.getAllRequestersItemRequests(requesterId, from, size);
    }

    @GetMapping("/all")
    public List<ItemRequestOutDto> getAllItemRequests(@RequestHeader(value = HEADER) long userId,
                                                      @RequestParam(value = "from", defaultValue = DEFAULT_FROM) @PositiveOrZero int from,
                                                      @RequestParam(value = "size", defaultValue = DEFAULT_SIZE) @Positive int size) {
        log.info("получен GET запрос на показ всех вещей userId={} from={} size={}", userId, from, size);
        return itemRequestService.getAllItemRequests(userId, from, size);
    }
}
