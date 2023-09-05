package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Collection<User> getAllUsers();

    User addUser(User user);

    Optional<User> getUserById(long id);

    User updateUser(User updatedUser);

    long deleteUserById(long id);

    boolean checkUserById(long id);
}
