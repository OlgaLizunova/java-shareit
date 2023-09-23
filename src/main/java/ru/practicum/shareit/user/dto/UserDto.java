package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private long id;
    @NotNull(message = "name must be not null")
    private String name;
    @NotNull(message = "email must be not null")
    @Email(message = "it's not email")
    private String email;
}
