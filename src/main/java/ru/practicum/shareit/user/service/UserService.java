package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    Collection<User> getAllUsers();

    User addUser(User user);

    User getUserById(long id);

    User updateUser(long userId, User updatedUser);

    long deleteUserById(long id);
}
