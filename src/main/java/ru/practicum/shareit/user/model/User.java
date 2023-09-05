package ru.practicum.shareit.user.model;

import lombok.*;

@Data
@AllArgsConstructor
public class User {
    private long id;
    private String name;
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
