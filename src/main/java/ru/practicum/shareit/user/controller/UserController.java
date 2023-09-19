package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        log.info("Получен POST на добавление нового пользователя с body={}", userDto);
        return userService.addUser(userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable(value = "userId") long id) {
        log.info("Получен GET запрос на показ пользователя с id={}", id);
        return userService.getUserById(id);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable(value = "userId") long userId) {
        log.info("получен PATCH запрос на обновление пользователя с id={}, requestBody={}", userId, userDto);
        return userService.updateUser(userId, userDto);
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        log.info("получен GET запрос на показ списка пользователей");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable("userId") Long id) {
        log.info("получен DELETE запрос на удаление пользователя с id= {}", id);
        userService.deleteUserById(id);
    }
}
