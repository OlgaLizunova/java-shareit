package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> addUser(@Valid @RequestBody UserDto userDto) {
        log.info("Получен POST на добавление нового пользователя с body={}", userDto);
        return userClient.addUser(userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable(value = "userId") long id) {
        log.info("Получен GET запрос на показ пользователя с id={}", id);
        return userClient.getUserById(id);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@RequestBody UserDto userDto, @PathVariable(value = "userId") long userId) {
        log.info("получен PATCH запрос на обновление пользователя с id={}, requestBody={}", userId, userDto);
        return userClient.updateUser(userId, userDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("получен GET запрос на показ списка пользователей");
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUserById(@PathVariable("userId") Long id) {
        log.info("получен DELETE запрос на удаление пользователя с id= {}", id);
        return userClient.deleteUserById(id);
    }
}
